import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * @author Jason Zhao
 * @author Isaac Rozen 
 * 
 * Description: Provides graphic interface for user to
 *         select a file for statistical analysis. Performs the analysis, plots
 *         the data, and allows the user to export the plot to a separate file.
 */
public class GUI extends Application {

	// scene heights and widths
	public final double SCENE_HEIGHT = 800;
	public static final double SCENE_WIDTH = 600;
	// image heights and widths
	public final double IMAGE_WIDTH = 400;
	public final double IMAGE_HEIGHT = 264;

	// size of the labels
	public final double LABEL_SIZE = 30;

	// sets the width for right side buttons
	public double buttonW = 200;

	public boolean usedLOESS = false;

	@Override
	/**
	 * @param Stage Primary stage.
	 * @throws Exception
	 */
	public void start(final Stage prime) throws Exception {

		prime.setTitle("Regression Toolkit");

		// generates Alert for error detection
		final Alert error = new Alert(AlertType.ERROR);
		error.setTitle("Error");
		error.setHeaderText("Error Found!");

		// handles where not all entities are fulfilled
		error.setContentText("File Not Selected!");

		// ToggleGroup for radio buttons - only at a time
		final ToggleGroup group = new ToggleGroup();

		// creates Buttons for GUI
		final Button fileButton = new Button("Select Excel File");
		fileButton.setPrefWidth(buttonW);

		final Button run = new Button("Read Excel File");
		run.setPrefWidth(buttonW);

		final Button exportLoc = new Button("Choose Export Location");
		exportLoc.setPrefWidth(buttonW);

		final Button plot = new Button("Generate Regression Model & Plot");
		plot.setPrefWidth(buttonW);

		final Button export = new Button("Export Plot");
		export.setPrefWidth(buttonW);

		final Button stats = new Button("Show Statistics");
		stats.setPrefWidth(buttonW);

		// creates Radio Button for different Regressions
		final RadioButton slr = new RadioButton();
		slr.setText("Simple Linear Regression");
		slr.setToggleGroup(group);

		final RadioButton wlr = new RadioButton();
		wlr.setText("Weighted Linear Regression");
		wlr.setToggleGroup(group);

		final RadioButton rlr = new RadioButton();
		rlr.setText("Robust Linear Regression");
		rlr.setToggleGroup(group);

		final RadioButton loess = new RadioButton();
		loess.setText("Local Regression");
		loess.setToggleGroup(group);

		// generates TextField for Local Regression
		final Label qLocal = new Label("q (0 to 1):");
		final TextField qLocalText = new TextField();

		// adds Label and Image of linear regression
		Label linearLabel = new Label("Linear Regression");
		linearLabel.setFont(Font.font("Verdana", FontWeight.BOLD, LABEL_SIZE));
		
		Image slrImage = new Image(getClass().getResourceAsStream("slr.png"));
		ImageView linear = new ImageView();
	
		linear.setFitWidth(IMAGE_WIDTH);
		linear.setFitHeight(IMAGE_HEIGHT);
		linear.setImage(slrImage);

		// adds Label and Image of Local Regression
		Label localLabel = new Label("Local Regression (LOESS)");
		localLabel.setFont(Font.font("Verdana", FontWeight.BOLD, LABEL_SIZE));
		
		Image llrImage = new Image(getClass().getResourceAsStream("llr.png"));
		ImageView local = new ImageView();
		
		local.setFitWidth(IMAGE_WIDTH);
		local.setFitHeight(IMAGE_HEIGHT);
		local.setImage(llrImage);

		// add Image of text instructions
		Image instructions = new Image(getClass().getResourceAsStream("text.jpg"));
		ImageView guide = new ImageView();
		guide.setImage(instructions);

		// for displaying location of file selected
		Label fileLabel = new Label("File:");
		final TextField fileText = new TextField();

		// for displaying saved locations
		final Label saveLabel = new Label("Exporting to:");
		final TextField saveText = new TextField();

		// for displaying export file name
		final Label fileNameLabel = new Label("File Name:");
		final TextField fileName = new TextField();

		// blank space, kludge used to space elements
		Label blankSpace = new Label("");

		// appending visual elements to pane
		VBox pane1 = new VBox();
		pane1.getChildren().addAll(fileLabel, fileText, fileButton, run);
		pane1.getChildren().addAll(blankSpace, plot, stats, exportLoc, export,
				saveLabel, saveText);
		pane1.getChildren().addAll(fileNameLabel, fileName);

		VBox paneMid = new VBox();
		paneMid.getChildren().addAll(linearLabel, linear, slr);
		paneMid.getChildren().addAll(wlr, rlr);

		VBox pane2 = new VBox();
		pane2.getChildren().addAll(localLabel, local);
		pane2.getChildren().addAll(loess, qLocal, qLocalText);

		HBox root = new HBox(10);
		root.getChildren().addAll(guide, paneMid, pane2, pane1);

		// creates scene
		Scene scene = new Scene(root);

		// disable buttons, reactivated as we go along
		run.setDisable(true);
		
		slr.setDisable(true);
		wlr.setDisable(true);
		rlr.setDisable(true);
		loess.setDisable(true);
		qLocal.setDisable(true);
		qLocalText.setDisable(true);
		plot.setDisable(true);

		stats.setDisable(true);
		exportLoc.setDisable(true);
		export.setDisable(true);
		saveLabel.setDisable(true);
		saveText.setDisable(true);
		fileNameLabel.setDisable(true);
		fileName.setDisable(true);

		// FileChooser for the file
		final FileChooser fileChooser = new FileChooser();

		// button for selecting file clicked
		fileButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent e) {
				// filters the file types to only Excel Files
				fileChooser.setTitle("Open File");
				fileChooser.getExtensionFilters().addAll(
						new ExtensionFilter("Excel Files", "*.xlsx"));

				// user selected file becomes displayed in textfield
				File selectedFile = fileChooser.showOpenDialog(prime);
				if (selectedFile != null) {
					fileText.setText(fileName(selectedFile));
					run.setDisable(false);
				}
			}
		});

		// HashMaps for title, x, y, weight values
		final HashMap<Integer, String> title = new HashMap<Integer, String>();
		final HashMap<Integer, Double> x = new HashMap<Integer, Double>();
		final HashMap<Integer, Double> y = new HashMap<Integer, Double>();
		final HashMap<Integer, Double> w = new HashMap<Integer, Double>();

		// calls on the excel reader
		final ExcelRead read = new ExcelRead();

		// Run button clicked activates action
		run.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(final ActionEvent e) {
				// captures case where not all textfields are filled
				try {
					// runs excel reader with all three inputs
					read.run(fileText.getText(), title, x, y, w);

				} catch (IOException e1) {
					// displays error and stack trace
					error.showAndWait();
					e1.printStackTrace();

					// exits program
					System.exit(0);
				}

				// enable buttons
				plot.setDisable(false);
				slr.setDisable(false);
				wlr.setDisable(false);
				rlr.setDisable(false);
				loess.setDisable(false);
				qLocal.setDisable(false);
				qLocalText.setDisable(false);
			}
		});

		final ArrayList<Double> xArray = new ArrayList<Double>();
		final ArrayList<Double> yArray = new ArrayList<Double>();
		final ArrayList<Double> wArray = new ArrayList<Double>();

		final LinearRegression linreg = new LinearRegression();
		final LocalRegression localreg = new LocalRegression();

		plot.setOnAction(new EventHandler<ActionEvent>() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void handle(final ActionEvent e) {
				final Alert error = new Alert(AlertType.ERROR);
				error.setTitle("Error");
				error.setHeaderText("Error Found!");

				prime.setTitle("Regression Plot");

				// defining the axes
				final NumberAxis xAxis = new NumberAxis();
				final NumberAxis yAxis = new NumberAxis();
				xAxis.setLabel(title.get(0));
				yAxis.setLabel(title.get(1));

				// creating the chart
				final LineChart<Number, Number> sc = new LineChart<Number, Number>(
						xAxis, yAxis);
				sc.setTitle("Regression Plot");

				// defining a series for scattered data
				XYChart.Series series = new XYChart.Series();
				series.setName("Data Points");

				// defining the regression series
				XYChart.Series reg = new XYChart.Series();

				reg.setName("Regression Line");

				// transfers Hashmap values into Arraylist
				for (int i = 0; i < x.size(); i++) {
					xArray.add(i, x.get(i + 1));
					yArray.add(i, y.get(i + 1));
					if (!w.isEmpty()) {
						wArray.add(i, w.get(i + 1));
					}
				}

				// 2D Double array for storing x and y into dataset
				int columns = 2; // 2 columns in the dataset
				final double[][] dataSet = new double[columns][x.size()];
				final double[] weight = new double[x.size()];

				// 2D array for local regression
				double[][] slopeInts = null;
				double[][] localSet = null;

				// transfers the ArrayList data into 2D arrays
				for (int index = 0; index < x.size(); index++) {
					dataSet[0][index] = xArray.get(index);
					dataSet[1][index] = yArray.get(index);
					if (!w.isEmpty()) {
						weight[index] = wArray.get(index);
					}
				}

				// plots different regressions depending on which user selects
				if (group.getSelectedToggle() == slr) {
					// running SIMPLE regression
					linreg.SLR(dataSet);
				} else if (group.getSelectedToggle() == rlr) {
					// running ROBUST regression
					linreg.RLR(dataSet);
				} else if (group.getSelectedToggle() == wlr) {
					// running WEIGHTED regression
					if (w.isEmpty()) {
						error.setContentText("No column of weights found!");
						error.showAndWait();
						return;
					} else if (weight.length != dataSet[0].length) {
						error.setContentText("Check that each x-y pair has "
								+ "an associated weight!");
						error.showAndWait();
						return;
					}

					linreg.WLR(dataSet, weight);
				} else if (group.getSelectedToggle() == loess) {
					// running LOCAL regression
					String qString = qLocalText.getText();

					// check if q textfield is filled
					if (qString.isEmpty()) {
						error.setContentText("Please input a value for q!");
						error.showAndWait();
						return;
					}

					double q = Double.parseDouble(qString);

					// check q is valid
					if (q > 1.0 || q < 0.0) {
						error.setContentText("Please input a value"
								+ " between 0 and 1 for q!");
						error.showAndWait();
						return;
					}

					// check if we have enough data points to plot!
					int minPoints = 4; // at least four points needed
					if (dataSet[0].length < minPoints) {
						error.setContentText("Not enough points to use LOESS!");
						error.showAndWait();
						return;
					}

					usedLOESS = true;
					slopeInts = localreg.LOESS(dataSet, q);
					localSet = localreg.plotLOESS(dataSet, slopeInts);
				} else {
					error.setContentText("Please select a mode of regression!");
					error.showAndWait();
					return;
				}

				// sort the dataset by x with associated x-y values
				linreg.xsort(dataSet);

				// populating the series with data
				for (int i = 1; i <= x.size(); i++) {
					series.getData().add(new XYChart.Data(x.get(i), y.get(i)));
				}

				// if not local regression
				if (group.getSelectedToggle() != loess) {
					// drawing the regression line by connecting initial and final point
					reg.getData().add(new XYChart.Data(0, linreg.SampleStats[1]));
					reg.getData().add(new XYChart.Data(dataSet[0][dataSet[0].length - 1],
							linreg.SampleStats[0] * dataSet[0][dataSet[0].length - 1]
											+ linreg.SampleStats[1]));
				} else {
					for (int i = 0; i < x.size(); i++) {
						reg.getData().add(new XYChart.Data(localSet[0][i],
								localSet[1][i]));
					}
				}

				// adds both scatter points and lines to the chart
				sc.getData().add(series);
				sc.getData().add(reg);
				sc.setAnimated(true);
				sc.setCreateSymbols(true);

				// defines new scene
				final Scene scene2 = new Scene(sc, SCENE_HEIGHT, SCENE_WIDTH);

				// connects css to generate overlay effect
				scene2.getStylesheets().add(
						getClass().getResource("root.css").toExternalForm());

				// defines new stage
				final Stage second = new Stage();

				second.setScene(scene2);
				second.show();

				// activates export location and stats button
				exportLoc.setDisable(false);
				stats.setDisable(false);
				saveLabel.setDisable(false);
				saveText.setDisable(false);

				// exports the seen chart to a file
				exportLoc.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(final ActionEvent e) {
						// allows user to select directory and updates textfield
						final DirectoryChooser directoryChooser = new DirectoryChooser();
						final File selectedDirectory = directoryChooser.showDialog(prime);

						if (selectedDirectory != null) {
							saveText.setText(fileName(selectedDirectory));
							export.setDisable(false);

							// activate file name buttons
							fileNameLabel.setDisable(false);
							fileName.setDisable(false);
						} else {
							error.setContentText("Directory not selected!");
							error.showAndWait();
						}
					}
				});

				// Exports to chosen location
				export.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(final ActionEvent e) {

						WritableImage wim = new WritableImage((int) scene2.getWidth(),
								(int) scene2.getHeight());

						(scene2).snapshot(wim);

						File file = null;
						if (!fileName.getText().isEmpty()) {
							file = new File(saveText.getText() + "\\" + fileName.getText()
									+ ".png");
						} else {
							error.setContentText("No File Name!");
							error.showAndWait();
							return;
						}
						try {
							ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
						} catch (IOException e2) {
							error.setContentText("Couldn't export file!");
							error.showAndWait();
							e2.printStackTrace();
							// exits program
							System.exit(0);
						}

						Alert success = new Alert(AlertType.INFORMATION);
						success.setTitle("Success");
						success.setHeaderText("Export successful!");
						success.setContentText(fileName.getText()
								+ ".png exported successfully!");
						success.showAndWait();
					}
				});

				// shows the statistics for the regression analysis
				stats.setOnAction(new EventHandler<ActionEvent>() {
					public void handle(final ActionEvent e) {
						// Ordering: Beta, Alpha, r^2, avgx, avgy, stdx, stdy
						// The regression line is given by y = alpha + beta(x)
						
						// spacings between label and value
						int space = 10;
						
						// numeric representations of stats element
						int betan = 0;
						int alphan = 1;
						int rSquaren = 2;
						int avgxn = 3;
						int avgyn = 4;
						int stdxn = 5;
						int stdyn = 6;
						
						HBox root = new HBox(space);
						VBox sample = new VBox(1);
						VBox value = new VBox(1);

						Label beta = new Label("Beta (slope):");
						Label alpha = new Label("Alpha (intercept):");
						Label rSquare = new Label("r^2:");
						Label avgx = new Label("Average x:");
						Label avgy = new Label("Average y:");
						Label stdx = new Label("Standard Deviation, x:");
						Label stdy = new Label("Standard Deviation, y:");

						sample.getChildren().addAll(beta, alpha, rSquare);
						sample.getChildren().addAll(avgx, avgy, stdx, stdy);

						if (usedLOESS) {
							Label betaV = new Label("" + localreg.SampleStats[betan]);
							Label alphaV = new Label("" + localreg.SampleStats[alphan]);
							Label rSquareV = new Label("" + localreg.SampleStats[rSquaren]);
							Label avgxV = new Label("" + localreg.SampleStats[avgxn]);
							Label avgyV = new Label("" + localreg.SampleStats[avgyn]);
							Label stdxV = new Label("" + localreg.SampleStats[stdxn]);
							Label stdyV = new Label("" + localreg.SampleStats[stdyn]);
							value.getChildren().addAll(betaV, alphaV, rSquareV);
							value.getChildren().addAll(avgxV, avgyV, stdxV, stdyV);
						} else {
							Label betaV = new Label("" + linreg.SampleStats[betan]);
							Label alphaV = new Label("" + linreg.SampleStats[alphan]);
							Label rSquareV = new Label("" + linreg.SampleStats[rSquaren]);
							Label avgxV = new Label("" + linreg.SampleStats[avgxn]);
							Label avgyV = new Label("" + linreg.SampleStats[avgyn]);
							Label stdxV = new Label("" + linreg.SampleStats[stdxn]);
							Label stdyV = new Label("" + linreg.SampleStats[stdyn]);
							value.getChildren().addAll(betaV, alphaV, rSquareV);
							value.getChildren().addAll(avgxV, avgyV, stdxV, stdyV);
						}

						root.getChildren().addAll(sample, value);

						// defines new scene
						final Scene scene3 = new Scene(root);

						// defines new stage
						final Stage statStage = new Stage();

						statStage.setScene(scene3);
						statStage.setTitle("Sample Statistics");
						statStage.show();
					}
				});
			}
		});

		// apply scene onto stage
		prime.setScene(scene);

		// displays the stage
		prime.show();

	}

	/**
	 * @param file
	 *          File of interested
	 * @return String of the location of the file Provides location of the string
	 *         for passing into XlsxReader
	 */
	public String fileName(File file) {

		return file.getPath();

	}

	/**
	 * @param args
	 *          Main method runs the program
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
