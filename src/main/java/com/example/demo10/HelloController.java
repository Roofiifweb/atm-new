package com.example.demo10;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HelloController extends Application {

    private static final String USER_FILE = "pengguna.txt";
    private static final String STRUK_FILE = "struk.txt";
    private static Map<String, UserData> userDataMap;

    public static void main(String[] args) {
        loadUserData();
        launch(args);
    }

    private static void loadUserData() {
        userDataMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String pin = parts[0];
                    String nama = parts[1];
                    double saldo = Double.parseDouble(parts[2]);
                    userDataMap.put(pin, new UserData(nama, saldo));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ATM Bank Application");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20, 50, 50, 50));

        Label welcomeLabel = new Label("Selamat datang di ATM");
        vbox.getChildren().add(welcomeLabel);

        // PIN Input
        PasswordField pinField = new PasswordField();
        pinField.setPromptText("Masukkan PIN Anda");
        vbox.getChildren().add(pinField);

        // Nama Pengguna Input
        TextField usernameField = new TextField();
        usernameField.setPromptText("Masukkan Nama Pengguna Anda");
        vbox.getChildren().add(usernameField);

        // Buttons
        Button checkBalanceButton = new Button("Cek Saldo");
        Button withdrawButton = new Button("Tarik Tunai");
        Button depositButton = new Button("Setor Tunai");
        Button printReceiptButton = new Button("Cetak Struk");

        vbox.getChildren().addAll(checkBalanceButton, withdrawButton, depositButton, printReceiptButton);

        Scene scene = new Scene(vbox, 300, 300);
        stage.setScene(scene);
        stage.show();

        // Event Handlers
        checkBalanceButton.setOnAction(e -> cekSaldo(pinField.getText(), usernameField.getText()));
        withdrawButton.setOnAction(e -> tarikTunai(pinField.getText(), usernameField.getText()));
        depositButton.setOnAction(e -> setorTunai(pinField.getText(), usernameField.getText()));
        printReceiptButton.setOnAction(e -> cetakStruk(pinField.getText(), usernameField.getText()));
    }

    private static void cekSaldo(String pin, String username) {
        if (validateUser(pin, username)) {
            showAlert("Saldo Anda: Rp" + userDataMap.get(pin).getSaldo());
        } else {
            showAlert("PIN salah atau pengguna tidak ditemukan.");
        }
    }

    private static void tarikTunai(String pin, String username) {
        if (validateUser(pin, username)) {
            double jumlah = promptAmount("Masukkan jumlah uang yang ingin ditarik");
            if (jumlah > userDataMap.get(pin).getSaldo() || jumlah <= 0 || jumlah % 50000 != 0) {
                showAlert("Jumlah penarikan tidak valid atau saldo tidak mencukupi atau bukan kelipatan 50,000.");
            } else {
                userDataMap.get(pin).tarikTunai(jumlah);
                showAlert("Penarikan berhasil. Saldo tersisa: Rp" + userDataMap.get(pin).getSaldo());
                updateUserDataFile();
            }
        }
    }

    private static void setorTunai(String pin, String username) {
        if (validateUser(pin, username)) {
            double jumlah = promptAmount("Masukkan jumlah uang yang ingin disetor");
            if (jumlah <= 0 || jumlah % 50000 != 0) {
                showAlert("Jumlah setoran tidak valid atau bukan kelipatan 50,000.");
            } else {
                userDataMap.get(pin).setorTunai(jumlah);
                showAlert("Setoran berhasil. Saldo baru: Rp" + userDataMap.get(pin).getSaldo());
                updateUserDataFile();
            }
        }
    }

    private static void cetakStruk(String pin, String username) {
        if (validateUser(pin, username)) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(STRUK_FILE))) {
                writer.println("Struk Transaksi");
                writer.println("Tanggal: " + java.time.LocalDate.now());
                writer.println("Waktu: " + java.time.LocalTime.now());
                writer.println("Detail Transaksi:");
                writer.println("Jumlah yang Ditarik/Setor: RpX.X");
                writer.println("Saldo Tersisa: Rp" + userDataMap.get(pin).getSaldo());
                showAlert("Struk berhasil dicetak. Lihat file " + STRUK_FILE);
            } catch (IOException e) {
                showAlert("Error mencetak struk: " + e.getMessage());
            }
        }
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static double promptAmount(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Masukkan Jumlah");
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);

        try {
            return Double.parseDouble(dialog.showAndWait().orElse("0"));
        } catch (NumberFormatException e) {
            showAlert("Masukkan jumlah yang valid.");
            return 0;
        }
    }

    private static boolean validateUser(String pin, String username) {
        UserData userData = userDataMap.get(pin);
        return userData != null && userData.getUsername().equals(username);
    }

    private static void updateUserDataFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_FILE))) {
            for (Map.Entry<String, UserData> entry : userDataMap.entrySet()) {
                String line = entry.getKey() + "|" + entry.getValue().getUsername() + "|" + entry.getValue().getSaldo();
                writer.println(line);
            }
        } catch (IOException e) {
            showAlert("Error mengupdate data pengguna: " + e.getMessage());
        }
    }

    private static class UserData {
        private String username;
        private double saldo;

        public UserData(String username, double saldo) {
            this.username = username;
            this.saldo = saldo;
        }

        public String getUsername() {
            return username;
        }

        public double getSaldo() {
            return saldo;
        }

        public void tarikTunai(double jumlah) {
            saldo -= jumlah;
        }

        public void setorTunai(double jumlah) {
            saldo += jumlah;
        }
    }
}
