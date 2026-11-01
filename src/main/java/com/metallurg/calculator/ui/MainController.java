package com.metallurg.calculator.ui;

import com.metallurg.calculator.MainApp;
import com.metallurg.calculator.dao.CalculationDao;
import com.metallurg.calculator.dao.EquipmentDao;
import com.metallurg.calculator.db.DatabaseSeeder;
import com.metallurg.calculator.model.AkosInput;
import com.metallurg.calculator.model.AkosResult;
import com.metallurg.calculator.model.Equipment;
import com.metallurg.calculator.model.MnlzInput;
import com.metallurg.calculator.model.MnlzResult;
import com.metallurg.calculator.model.SteelComposition;
import com.metallurg.calculator.service.AkosCalculator;
import com.metallurg.calculator.service.DataGenerator;
import com.metallurg.calculator.service.ExportService;
import com.metallurg.calculator.service.FileImportService;
import com.metallurg.calculator.service.MnlzCalculator;
import com.metallurg.calculator.util.Validation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Главный контроллер GUI. */
public class MainController {

    private final AkosCalculator akosCalculator = new AkosCalculator();
    private final MnlzCalculator mnlzCalculator = new MnlzCalculator();
    private final DataGenerator dataGenerator = new DataGenerator();
    private final FileImportService fileImportService = new FileImportService();
    private final ExportService exportService = new ExportService();
    private final CalculationDao calculationDao = new CalculationDao();
    private final EquipmentDao equipmentDao = new EquipmentDao();

    private String currentUsername = "guest";
    private Integer currentUserId;
    private AkosResult lastAkosResult;
    private MnlzResult lastMnlzResult;
    private AkosInput lastAkosInput;
    private MnlzInput lastMnlzInput;

    @FXML private Label userLabel;
    @FXML private Label statusLabel;
    @FXML private ToggleGroup inputModeGroup;
    @FXML private RadioButton manualRadio;
    @FXML private RadioButton randomRadio;
    @FXML private RadioButton fileRadio;
    @FXML private CheckBox saveToDbCheck;

    @FXML private ComboBox<String> akosGradeCombo;
    @FXML private TextField akosMassField;
    @FXML private TextField bC, bMn, bSi, bS, bP, bCr, bNi, bMo, bAl;
    @FXML private TextField tC, tMn, tSi, tS, tP, tCr, tNi, tMo, tAl;
    @FXML private TextField akosGenCount;
    @FXML private TextArea akosResultArea;

    @FXML private ComboBox<String> mnlzGradeCombo;
    @FXML private TextField mnlzMassField, mnlzWidthField, mnlzThickField, mnlzSpeedField, mnlzTimeField, mnlzKzField;
    @FXML private TextField mnlzGenCount;
    @FXML private ChoiceBox<String> mnlzSectionChoice;
    @FXML private TextArea mnlzResultArea;

    @FXML private TextField filterField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private CheckBox sortAscCheck;
    @FXML private TableView<CalculationDao.MnlzRow> mnlzTable;
    @FXML private TableView<CalculationDao.AkosRow> akosTable;

    @FXML private BarChart<String, Number> poolChart;

    @FXML private TextField equipmentSearch;
    @FXML private ComboBox<String> equipmentTypeCombo;
    @FXML private TableView<Equipment> equipmentTable;

    @FXML private TextArea helpArea;

    @FXML
    private void initialize() {
        List<String> grades = List.of("25Х2Н4МА", "25ХГСА", "40ХГНМ", "Ст3сп", "35ГС", "70", "40Х");
        akosGradeCombo.setItems(FXCollections.observableArrayList(grades));
        mnlzGradeCombo.setItems(FXCollections.observableArrayList(grades));
        mnlzSectionChoice.setItems(FXCollections.observableArrayList("Сляб", "Сортовая"));
        mnlzSectionChoice.getSelectionModel().selectFirst();
        mnlzSectionChoice.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> autoKz());

        sortCombo.setItems(FXCollections.observableArrayList(
                "id", "steel_grade", "converter_mass_t", "strands", "liquid_pool_m", "radius_m", "created_at"));
        sortCombo.getSelectionModel().select("id");

        setupMnlzTable();
        setupAkosTable();
        setupEquipmentTable();

        equipmentTypeCombo.setItems(FXCollections.observableArrayList(
                "Все", "АКОС", "МНЛЗ", "Конвертер", "Промковш", "Кристаллизатор", "Ковш"));
        equipmentTypeCombo.getSelectionModel().selectFirst();

        helpArea.setText(loadHelp());
        onAkosExample();
        onMnlzExample();
        try {
            refreshTables();
            onEquipmentReset();
            onRefreshChart();
            status("Система готова");
        } catch (Exception e) {
            status("Интерфейс загружен, БД: " + e.getMessage());
        }
    }

    public void setCurrentUser(String username) {
        this.currentUsername = username;
        userLabel.setText("Пользователь: " + username);
        // id подтянем лениво при сохранении при необходимости — для простоты null у seed/admin ок
        this.currentUserId = null;
    }

    @FXML
    private void onAkosExample() {
        fillAkosForm(DatabaseSeeder.typicalAkos());
        status("Загружен пример АКОС из §7.2");
    }

    @FXML
    private void onMnlzExample() {
        fillMnlzForm(DatabaseSeeder.typicalMnlz());
        status("Загружен пример МНЛЗ из §8.2");
    }

    @FXML
    private void onAkosCalculate() {
        try {
            if (randomRadio.isSelected()) {
                onAkosGenerate();
                return;
            }
            if (fileRadio.isSelected()) {
                onImportAkos();
                return;
            }
            AkosInput input = readAkosForm();
            input.setSourceMode("manual");
            runAkos(List.of(input));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onMnlzCalculate() {
        try {
            if (randomRadio.isSelected()) {
                onMnlzGenerate();
                return;
            }
            if (fileRadio.isSelected()) {
                onImportMnlz();
                return;
            }
            MnlzInput input = readMnlzForm();
            input.setSourceMode("manual");
            runMnlz(List.of(input));
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onAkosGenerate() {
        try {
            int n = Validation.requirePositiveInt(akosGenCount.getText(), "N");
            List<AkosInput> list = dataGenerator.generateAkos(n);
            fillAkosForm(list.getFirst());
            runAkos(list);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onMnlzGenerate() {
        try {
            int n = Validation.requirePositiveInt(mnlzGenCount.getText(), "N");
            List<MnlzInput> list = dataGenerator.generateMnlz(n);
            fillMnlzForm(list.getFirst());
            runMnlz(list);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onImportAkos() {
        Path path = chooseOpen("Импорт АКОС CSV");
        if (path == null) {
            return;
        }
        try {
            List<AkosInput> list = fileImportService.importAkos(path);
            fillAkosForm(list.getFirst());
            runAkos(list);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onImportMnlz() {
        Path path = chooseOpen("Импорт МНЛЗ CSV");
        if (path == null) {
            return;
        }
        try {
            List<MnlzInput> list = fileImportService.importMnlz(path);
            fillMnlzForm(list.getFirst());
            runMnlz(list);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void refreshTables() {
        try {
            String filter = filterField.getText();
            String sort = sortCombo.getValue();
            boolean asc = sortAscCheck.isSelected();
            mnlzTable.setItems(FXCollections.observableArrayList(calculationDao.findMnlz(filter, sort, asc)));
            akosTable.setItems(FXCollections.observableArrayList(calculationDao.findAkos(filter)));
            status("Таблицы обновлены");
        } catch (SQLException e) {
            showError("Ошибка чтения БД: " + e.getMessage());
        }
    }

    @FXML
    private void onFilter() {
        refreshTables();
        onRefreshChart();
    }

    @FXML
    private void onDeleteSelected() {
        try {
            CalculationDao.MnlzRow m = mnlzTable.getSelectionModel().getSelectedItem();
            if (m != null) {
                calculationDao.deleteMnlz(m.id());
            }
            CalculationDao.AkosRow a = akosTable.getSelectionModel().getSelectedItem();
            if (a != null) {
                calculationDao.deleteAkos(a.id());
            }
            if (m == null && a == null) {
                showError("Выберите запись в таблице");
                return;
            }
            refreshTables();
            onRefreshChart();
            status("Запись удалена");
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onRefreshChart() {
        try {
            poolChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Lл");
            List<CalculationDao.MnlzRow> rows = calculationDao.findMnlz(filterField.getText(), "id", false);
            int limit = Math.min(15, rows.size());
            for (int i = 0; i < limit; i++) {
                CalculationDao.MnlzRow r = rows.get(i);
                series.getData().add(new XYChart.Data<>(r.steelGrade() + "#" + r.id(), r.liquidPool()));
            }
            poolChart.getData().add(series);
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onExportCsv() {
        Path path = chooseSave("Экспорт CSV", "*.csv");
        if (path == null) {
            return;
        }
        try {
            exportService.exportMnlzCsv(path, currentMnlzRows());
            status("CSV сохранён: " + path.getFileName());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onExportXlsx() {
        Path path = chooseSave("Экспорт XLSX", "*.xlsx");
        if (path == null) {
            return;
        }
        try {
            exportService.exportMnlzXlsx(path, currentMnlzRows());
            status("XLSX сохранён: " + path.getFileName());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onExportPdf() {
        Path path = chooseSave("Экспорт PDF", "*.pdf");
        if (path == null) {
            return;
        }
        try {
            exportService.exportMnlzPdf(path, currentMnlzRows(), "Отчёт расчётов МНЛЗ");
            status("PDF сохранён: " + path.getFileName());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onExportChartPdf() {
        Path path = chooseSave("График PDF", "*.pdf");
        if (path == null) {
            return;
        }
        try {
            List<String> lines = new ArrayList<>();
            lines.add("График глубины лунки жидкой фазы (Lл) по последним расчётам МНЛЗ.");
            for (CalculationDao.MnlzRow r : currentMnlzRows().stream().limit(20).toList()) {
                lines.add(r.steelGrade() + " #" + r.id() + ": Lл = " + r.liquidPool() + " м, R = " + r.radius() + " м");
            }
            exportService.exportTextReportPdf(path, "График / отчёт МНЛЗ", lines);
            status("PDF графика сохранён");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onReportCurrent() {
        Path path = chooseSave("Отчёт текущего расчёта", "*.pdf");
        if (path == null) {
            return;
        }
        try {
            List<String> lines = new ArrayList<>();
            if (lastAkosResult != null && lastAkosInput != null) {
                lines.add("АКОС, марка " + lastAkosInput.getSteelGrade());
                lines.add("Масса плавки: " + lastAkosInput.getMeltMassKg() + " кг");
                for (Map.Entry<String, Double> e : lastAkosResult.getFerroalloyMassKg().entrySet()) {
                    lines.add(e.getKey() + ": " + e.getValue() + " кг");
                }
                lines.add("Итоговая масса: " + lastAkosResult.getFinalMassKg() + " кг");
                lines.add(" ");
            }
            if (lastMnlzResult != null && lastMnlzInput != null) {
                lines.add("МНЛЗ, марка " + lastMnlzInput.getSteelGrade());
                lines.add("Ручьи n = " + lastMnlzResult.getStrands());
                lines.add("Lл = " + lastMnlzResult.getLiquidPoolDepthM() + " м");
                lines.add("R = H = " + lastMnlzResult.getRadiusM() + " м");
                lines.add("Тип: " + lastMnlzResult.getMachineType());
            }
            if (lines.isEmpty()) {
                showError("Сначала выполните расчёт");
                return;
            }
            exportService.exportTextReportPdf(path, "Отчёт текущего расчёта", lines);
            status("Отчёт сохранён");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEquipmentFilter() {
        try {
            String type = equipmentTypeCombo.getValue();
            String q = equipmentSearch.getText();
            List<Equipment> list;
            if (type != null && !"Все".equals(type) && (q == null || q.isBlank())) {
                list = equipmentDao.filterByType(type);
            } else if (q != null && !q.isBlank()) {
                list = equipmentDao.search(q);
                if (type != null && !"Все".equals(type)) {
                    list = list.stream().filter(e -> type.equals(e.getType())).toList();
                }
            } else {
                list = equipmentDao.findAll();
            }
            equipmentTable.setItems(FXCollections.observableArrayList(list));
            status("Оборудование: " + list.size() + " записей");
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEquipmentReset() {
        equipmentSearch.clear();
        equipmentTypeCombo.getSelectionModel().selectFirst();
        onEquipmentFilter();
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("ИС «Калькулятор металлурга»");
        alert.setContentText("Вариант 4. JavaFX + SQLite.\nИсточник: Токовой О.К. Производство стали и сплавов.");
        alert.showAndWait();
    }

    @FXML
    private void onLogout() {
        try {
            MainApp.showLogin();
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    private void runAkos(List<AkosInput> inputs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (AkosInput input : inputs) {
            AkosResult result = akosCalculator.calculate(input);
            lastAkosInput = input;
            lastAkosResult = result;
            if (saveToDbCheck.isSelected()) {
                calculationDao.saveAkos(currentUserId, input, result);
            }
            sb.append("Марка: ").append(input.getSteelGrade())
                    .append(", масса: ").append(input.getMeltMassKg()).append(" кг\n");
            sb.append("Расход материалов:\n");
            result.getFerroalloyMassKg().forEach((k, v) ->
                    sb.append("  • ").append(k).append(": ").append(v).append(" кг\n"));
            SteelComposition c = result.getFinalCompositionPct();
            sb.append(String.format(
                    "Итоговый состав, %%: C=%.3f Mn=%.3f Si=%.3f S=%.3f P=%.3f Cr=%.3f Ni=%.3f Mo=%.3f Al=%.3f%n",
                    c.getC(), c.getMn(), c.getSi(), c.getS(), c.getP(), c.getCr(), c.getNi(), c.getMo(), c.getAl()));
            sb.append("Итоговая масса: ").append(result.getFinalMassKg()).append(" кг\n");
            sb.append("----------------------------------------\n");
        }
        akosResultArea.setText(sb.toString());
        refreshTables();
        status("АКОС: обработано " + inputs.size());
    }

    private void runMnlz(List<MnlzInput> inputs) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (MnlzInput input : inputs) {
            MnlzResult result = mnlzCalculator.calculate(input);
            lastMnlzInput = input;
            lastMnlzResult = result;
            if (saveToDbCheck.isSelected()) {
                calculationDao.saveMnlz(currentUserId, input, result);
            }
            sb.append("Марка: ").append(input.getSteelGrade()).append('\n');
            sb.append("Число ручьев n = ").append(result.getStrands()).append('\n');
            sb.append("Глубина лунки Lл = ").append(result.getLiquidPoolDepthM()).append(" м\n");
            sb.append("Радиус R = высота H = ").append(result.getRadiusM()).append(" м\n");
            sb.append("Rmin (практ.) = ").append(result.getMinRadiusPracticeM()).append(" м\n");
            sb.append("Gз = ").append(result.getMassRateTPerMin()).append(" т/мин\n");
            sb.append("Рекомендуемый тип: ").append(result.getMachineType()).append('\n');
            sb.append("----------------------------------------\n");
        }
        mnlzResultArea.setText(sb.toString());
        refreshTables();
        onRefreshChart();
        status("МНЛЗ: обработано " + inputs.size());
    }

    private AkosInput readAkosForm() {
        AkosInput in = new AkosInput();
        in.setSteelGrade(Validation.requireText(akosGradeCombo.getEditor().getText().isBlank()
                ? akosGradeCombo.getValue() : akosGradeCombo.getEditor().getText(), "марка"));
        in.setMeltMassKg(Validation.requirePositive(akosMassField.getText(), "масса плавки"));
        in.setBefore(new SteelComposition(
                num(bC, "C до"), num(bMn, "Mn до"), num(bSi, "Si до"), num(bS, "S до"), num(bP, "P до"),
                num(bCr, "Cr до"), num(bNi, "Ni до"), num(bMo, "Mo до"), num(bAl, "Al до")));
        in.setTarget(new SteelComposition(
                num(tC, "C цел"), num(tMn, "Mn цел"), num(tSi, "Si цел"), num(tS, "S цел"), num(tP, "P цел"),
                num(tCr, "Cr цел"), num(tNi, "Ni цел"), num(tMo, "Mo цел"), num(tAl, "Al цел")));
        return in;
    }

    private MnlzInput readMnlzForm() {
        MnlzInput in = new MnlzInput();
        in.setSteelGrade(Validation.requireText(mnlzGradeCombo.getEditor().getText().isBlank()
                ? mnlzGradeCombo.getValue() : mnlzGradeCombo.getEditor().getText(), "марка"));
        in.setConverterMassT(Validation.requirePositive(mnlzMassField.getText(), "садка"));
        in.setWidthM(Validation.requirePositive(mnlzWidthField.getText(), "ширина"));
        in.setThicknessM(Validation.requirePositive(mnlzThickField.getText(), "толщина"));
        in.setCastingSpeedMPerMin(Validation.requirePositive(mnlzSpeedField.getText(), "скорость"));
        in.setCastingTimeMin(Validation.requirePositive(mnlzTimeField.getText(), "время"));
        in.setSolidificationCoeff(Validation.requirePositive(mnlzKzField.getText(), "kз"));
        return in;
    }

    private void fillAkosForm(AkosInput in) {
        akosGradeCombo.setValue(in.getSteelGrade());
        akosMassField.setText(String.valueOf(in.getMeltMassKg()));
        SteelComposition b = in.getBefore();
        SteelComposition t = in.getTarget();
        setComp(bC, bMn, bSi, bS, bP, bCr, bNi, bMo, bAl, b);
        setComp(tC, tMn, tSi, tS, tP, tCr, tNi, tMo, tAl, t);
    }

    private void fillMnlzForm(MnlzInput in) {
        mnlzGradeCombo.setValue(in.getSteelGrade());
        mnlzMassField.setText(String.valueOf(in.getConverterMassT()));
        mnlzWidthField.setText(String.valueOf(in.getWidthM()));
        mnlzThickField.setText(String.valueOf(in.getThicknessM()));
        mnlzSpeedField.setText(String.valueOf(in.getCastingSpeedMPerMin()));
        mnlzTimeField.setText(String.valueOf(in.getCastingTimeMin()));
        mnlzKzField.setText(String.valueOf(in.getSolidificationCoeff()));
        if (Math.abs(in.getWidthM() - in.getThicknessM()) < 1e-6) {
            mnlzSectionChoice.setValue("Сортовая");
        } else {
            mnlzSectionChoice.setValue("Сляб");
        }
    }

    private void autoKz() {
        try {
            if (mnlzWidthField.getText().isBlank() || mnlzThickField.getText().isBlank()) {
                return;
            }
            double B = Double.parseDouble(mnlzWidthField.getText().replace(',', '.'));
            double b = Double.parseDouble(mnlzThickField.getText().replace(',', '.'));
            if ("Сортовая".equals(mnlzSectionChoice.getValue())) {
                mnlzThickField.setText(String.valueOf(B));
                b = B;
            }
            mnlzKzField.setText(String.valueOf(MnlzCalculator.defaultSolidificationCoeff(B, b)));
        } catch (Exception ignored) {
            // пользователь ещё вводит данные
        }
    }

    private void setupMnlzTable() {
        mnlzTable.getColumns().setAll(List.of(
                recordCol("ID", 50, CalculationDao.MnlzRow::id),
                recordCol("Марка", 100, CalculationDao.MnlzRow::steelGrade),
                recordCol("Садка", 70, CalculationDao.MnlzRow::converterMassT),
                recordCol("Ручьи", 60, CalculationDao.MnlzRow::strands),
                recordCol("Lл", 70, CalculationDao.MnlzRow::liquidPool),
                recordCol("R", 70, CalculationDao.MnlzRow::radius),
                recordCol("Тип", 220, CalculationDao.MnlzRow::machineType),
                recordCol("Режим", 80, CalculationDao.MnlzRow::sourceMode)
        ));
    }

    private void setupAkosTable() {
        akosTable.getColumns().setAll(List.of(
                recordCol("ID", 50, CalculationDao.AkosRow::id),
                recordCol("Марка", 100, CalculationDao.AkosRow::steelGrade),
                recordCol("Масса", 90, CalculationDao.AkosRow::meltMassKg),
                recordCol("Режим", 80, CalculationDao.AkosRow::sourceMode),
                recordCol("Дата", 140, CalculationDao.AkosRow::createdAt)
        ));
    }

    private void setupEquipmentTable() {
        TableColumn<Equipment, Integer> id = beanCol("ID", "id", 50);
        TableColumn<Equipment, String> name = beanCol("Название", "name", 180);
        TableColumn<Equipment, String> type = beanCol("Тип", "type", 100);
        TableColumn<Equipment, Double> cap = beanCol("Садка, т", "capacityT", 80);
        TableColumn<Equipment, String> maker = beanCol("Производитель", "manufacturer", 120);
        TableColumn<Equipment, String> desc = beanCol("Описание", "description", 280);
        equipmentTable.getColumns().setAll(List.of(id, name, type, cap, maker, desc));
    }

    private <S, T> TableColumn<S, T> recordCol(String title, double width, Function<S, T> getter) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(getter.apply(cd.getValue())));
        c.setPrefWidth(width);
        return c;
    }

    private <S, T> TableColumn<S, T> beanCol(String title, String prop, double width) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    private List<CalculationDao.MnlzRow> currentMnlzRows() throws SQLException {
        return calculationDao.findMnlz(filterField.getText(), sortCombo.getValue(), sortAscCheck.isSelected());
    }

    private Path chooseOpen(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV / TXT", "*.csv", "*.txt"));
        var file = fc.showOpenDialog(MainApp.getPrimaryStage());
        return file == null ? null : file.toPath();
    }

    private Path chooseSave(String title, String ext) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext, ext));
        var file = fc.showSaveDialog(MainApp.getPrimaryStage());
        return file == null ? null : file.toPath();
    }

    private void setComp(TextField c, TextField mn, TextField si, TextField s, TextField p,
                         TextField cr, TextField ni, TextField mo, TextField al, SteelComposition x) {
        c.setText(str(x.getC()));
        mn.setText(str(x.getMn()));
        si.setText(str(x.getSi()));
        s.setText(str(x.getS()));
        p.setText(str(x.getP()));
        cr.setText(str(x.getCr()));
        ni.setText(str(x.getNi()));
        mo.setText(str(x.getMo()));
        al.setText(str(x.getAl()));
    }

    private static String str(double v) {
        return String.valueOf(v);
    }

    private double num(TextField field, String name) {
        return Validation.requireNonNegative(field.getText(), name);
    }

    private String loadHelp() {
        try (InputStream in = getClass().getResourceAsStream("/help/help.txt")) {
            if (in == null) {
                return "Справка не найдена";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Справка недоступна: " + e.getMessage();
        }
    }

    private void status(String text) {
        statusLabel.setText(text);
    }

    private void showError(String message) {
        statusLabel.setText("Ошибка");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Проверьте ввод");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
