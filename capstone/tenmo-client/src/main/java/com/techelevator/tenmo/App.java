package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.tenmo.services.UserService;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class App {
    private int balance;

    private static final String API_BASE_URL = "http://localhost:8080/";
    private UserService userService = new UserService();
    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private AuthenticatedUser currentUser;
    TransferService transferService = new TransferService();
    Account account;
    List<Transfer> transferList = new ArrayList<>();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    public AuthenticatedUser getCurrentUser() {
        return currentUser;
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 6) {
                listUsers();
            } else if (menuSelection == 69) {
                System.out.println(currentUser.getToken());
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
//        UserService userService = new UserService();
        int userId = Math.toIntExact(currentUser.getUser().getId());
        BigDecimal balance = userService.getBalance(currentUser, userId);
        System.out.println("--------------------------------------------");
        System.out.println("Your balance is $" + balance);
        System.out.println("--------------------------------------------");


    }

    private void viewTransferHistory() {
        listTransfers();

        Transfer[] transfers = transferService.getTransfersFromUserId(currentUser);
        int choice = -1;
        while (choice != 0) {
            if (transfers == null) {
                System.out.print("don't exist lol find friends");
                break;
            }
            if (transferList.size() == 0){
                break;
            }
            for (Transfer transfer : transfers) {
                int accountIdFrom = transfer.getAccountFrom();
                int accountIdTo = transfer.getAccountTo();
                User userFrom = userService.getUserByAccountId(currentUser, accountIdFrom);
                User userTo = userService.getUserByAccountId(currentUser, accountIdTo);
                choice = consoleService.promptForMenuSelection("Please enter transfer ID to view details (0 to cancel): ");
                Transfer transferToGet = transferService.getTransferById(currentUser, choice);
                int transferId = transferToGet.getTransferId();
                if (choice == 0) {
                    break;
                } else if (transferService.getTransferById(currentUser, transferId).getAmount() == null) {
                    System.out.println("Transfer doesn't exist");
                } else {
                    Transfer transferToDisplay = transferService.getTransferById(currentUser, choice);
                    if (transferToDisplay == null) {
                        System.out.println("Transfer does not exist");
                    } else {
                        System.out.println("------------");
                        System.out.println("ID: " + transferToDisplay.getTransferId());
                        System.out.println("From: " + userService.getUsernameByAccountId(currentUser, transferToDisplay.getAccountFrom()));
                        System.out.println("To: " + userService.getUsernameByAccountId(currentUser, transferToDisplay.getAccountTo()));
                        System.out.println("Type: Send");
//                                +  transferToDisplay.getTransferType());
                        System.out.println("Status: Approved");
//                                +  transferToDisplay.getTransferId());
                        System.out.println("Amount: " + transferToDisplay.getAmount());
                        break;

                        //TODO add type and status instead of hard coding what it is
                    }
                }
            }
        }

        System.out.println();
        System.out.println("--------------------------------------------");

    }

    private void viewPendingRequests() {
        Transfer[] transfers = transferService.viewPendingTransfers(currentUser);
        System.out.println("--------------------------------------------");
        System.out.println("Your transfers are " + Arrays.toString(transfers));
        if (transfers == null) {
            System.out.print("nothing lol");
        } else {
            for (Transfer transfer : transfers) {
                System.out.println(transfer.getAccountFrom());
            }
            System.out.println("--------------------------------------------");
        }
    }

    public void sendBucks() {
        Transfer transfer = new Transfer();
        User[] users = userService.listUsers(currentUser);
        System.out.println("-------------------------------------------");
        System.out.println("ID       :     Name");
        System.out.println();
        for (User user : users) {
            System.out.println(user.getId() + "     :     " + user.getUsername());
        }
        System.out.println("-------------------------------------------");
        int userIdToSend = 0;
        while (true) {
            userIdToSend = consoleService.promptForMenuSelection("Enter ID of user you are sending to (0 to cancel): ");
            if (userIdToSend == 0) {
                break;
            } else {
                User userToSend = userService.getUserById(currentUser, userIdToSend);
                if (userToSend == null) {
                    System.out.println("User does not exist");
                    break;
                } else if (Objects.equals(userToSend.getId(), currentUser.getUser().getId())) {
                    System.out.println("You can't send money to yourself");
                    break;
                }
                System.out.println("How much money do you want to send to ID: " + userToSend.getId()
                        + " User: " + userToSend.getUsername());
                BigDecimal amountToSend = consoleService.promptForBigDecimal("Enter amount: ");
                int currentUserId = Math.toIntExact(currentUser.getUser().getId());
                int accountFrom = Math.toIntExact(userService.getAccountById(currentUser, currentUserId).getAccountId());
                int accountTo = Math.toIntExact(userService.getAccountById(currentUser, userIdToSend).getAccountId());

                transfer.setAccountFrom(accountFrom);
                transfer.setAccountTo(accountTo);
                transfer.setAmount(amountToSend);

                boolean didItWork = false;

                if (userService.getBalance(currentUser, currentUserId).compareTo(amountToSend) < 0) {
                    System.out.println("You can't send more money than you have");
                } else {
                    didItWork = transferService.sendTEbucks(currentUser, accountFrom, accountTo, transfer);
                    System.out.println("You sent $" + amountToSend + " to " + userService.getUsernameByAccountId(currentUser, accountTo));
                    System.out.println("You now have $" + userService.getBalance(currentUser, currentUserId));
                }
                break;
            }
        }
    }


    public void requestBucks() {
//        balance = balance + amountToRequest;
//        return balance;
//		// TODO Auto-generated method stub
//
    }

    private void listUsers() {
        User[] users = userService.listUsers(currentUser);
        System.out.println("-------------------------------------------");
        System.out.println("ID      :      Name");
        System.out.println();
        for (User user : users) {
            System.out.println(user.getId() + "     :     " + user.getUsername());
        }
        System.out.println("-------------------------------------------");
    }

    private void listTransfers() {
        Transfer[] transfers = transferService.getListOfTransfers(currentUser);
        List<Transfer> transferList = new ArrayList<>();
        if (transfers.length == 0) {
            System.out.println("No transfers listed");
        } else {
            System.out.println("--------------------------------------------");
            System.out.println("Transfers ");
            System.out.println("ID      FROM/TO           Amount");

            for (Transfer transfer : transfers) {
                int currentUserId = Math.toIntExact(currentUser.getUser().getId());
                if (transfer.getAccountFrom() == userService.getAccountById(currentUser, currentUserId).getAccountId()) {
                    transferList.add(transfer);
                } else if (transfer.getAccountTo() == userService.getAccountById(currentUser, currentUserId).getAccountId()) {

                    transferList.add(transfer);
                }
            }

            if (transferList.size() == 0) {
                System.out.println("No transfers listed");
            } else {
                for (Transfer transfer : transferList) {
                    int currentUserId = Math.toIntExact(currentUser.getUser().getId());

                    if (transfer.getAccountFrom() == userService.getAccountById(currentUser, currentUserId).getAccountId()) {
                        System.out.println(transfer.getTransferId()
                                + "      To: "
                                + userService.getUsernameByAccountId(currentUser, transfer.getAccountTo())
                                + "      Amount: $"
                                + transfer.getAmount());
                    } else if (transfer.getAccountTo() == userService.getAccountById(currentUser, currentUserId).getAccountId()) {
                        System.out.println(transfer.getTransferId()
                                + "      From: "
                                + userService.getUsernameByAccountId(currentUser, transfer.getAccountFrom())
                                + "      Amount: $"
                                + transfer.getAmount());


                    }

                }

            }

        }
    }
}