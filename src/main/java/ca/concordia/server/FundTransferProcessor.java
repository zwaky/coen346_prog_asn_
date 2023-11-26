package ca.concordia.server;

public class FundTransferProcessor {
    private Account accountManager;

    public FundTransferProcessor(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public boolean processTransfer(int sourceAccountId, int sourceValue, int destinationAccountId,
            int destinationValue) {
        // Retrieve source and destination accounts
        Account sourceAccount = accountManager.findAccountById(sourceAccountId);
        Account destinationAccount = accountManager.findAccountById(destinationAccountId);

        // Check if both accounts exist
        if (sourceAccount == null || destinationAccount == null) {
            System.out.println("Invalid source or destination account.");
            return false;
        }

        // Check if the source account has enough balance for the transfer
        if (sourceAccount.getBalance() < sourceValue) {
            System.out.println("Insufficient funds in the source account.");
            return false;
        }

        // Perform the fund transfer
        sourceAccount.withdraw(sourceValue);
        destinationAccount.deposit(destinationValue);

        // Print a confirmation message
        System.out.println("Transfer successful: " + sourceValue + " units from account " + sourceAccountId +
                " to account " + destinationAccountId);

        return true;
    }
}
