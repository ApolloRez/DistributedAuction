package shared;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Message implements Serializable {

    private static final long serialVersionUID = -1195974328835714539L;
    private final Double amount;
    private final UUID sender;
    private final UUID accountId;
    private final Command command;
    private final Response response;
    private final List<NetInfo> netInfo;

    /**
     * Message constructor that takes a builder object.
     *
     * @param builder Builder
     */
    private Message(Builder builder) {
        this.amount = builder.amount;
        this.sender = builder.sender;
        this.accountId = builder.accountId;
        this.command = builder.command;
        this.netInfo = builder.netInfo;
        this.response = builder.response;
    }

    public static void main(String[] args) {
        Message message = new Builder()
                .accountId(UUID.randomUUID())
                .amount(.32)
                .command(Command.HOLD)
                .send(UUID.randomUUID());
        System.out.println(message.getAccountId());
        System.out.println(message.getAmount());
    }

    /**
     * Get the response.
     * @return enum
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Get the amount.
     * @return Double
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Get the UUID of the message sender.
     * @return UUID
     */
    public UUID getSender() {
        return sender;
    }

    /**
     * Get the UUID of the target.
     *
     * @return UUID
     */
    public UUID getAccountId() {
        return accountId;
    }

    /**
     * Get the message command.
     * @return enum
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Get a list of the NetInfo of the auction houses.
     *
     * @return List<NetInfo></NetInfo>
     */
    public List<NetInfo> getNetInfo() {
        return netInfo;
    }

    /**
     * Return a string of non null values.
     *
     * @return String
     */
    @Override
    public String toString() {
        String message = "{";
        if (amount != null) message += ("amount=" + amount);
        if (sender != null) message += ("\n\t\tsender=" + sender);
        if (accountId != null) message += ("\n\t\taccountId=" + accountId);
        if (command != null) message += ("\n\t\tcommand=" + command);
        if (response != null) message += ("\n\t\tresponse=" + response);
        if (netInfo != null) message += ("\n\t\tnetInfo=" + netInfo);
        return message + "}";
    }

    /**
     * Enum representing responses from the bank.
     * SUCCESS - The transaction was successful.
     * ERROR - Something somewhere went wrong.
     * INSUFFICIENT_FUNDS - Not enough funds.
     * INVALID_PARAMETERS - The parameters were wrong somewhere.
     */
    public enum Response {
        SUCCESS,
        ERROR,
        INSUFFICIENT_FUNDS,
        INVALID_PARAMETERS
    }

    /**
     * Enum representing message commands.
     *         DEPOSIT - Deposit an amount into the senderId account.
     *         TRANSFER - Transfer an amount from senderId to accountId.
     *         HOLD - Hold an amount on an accountId.
     *         RELEASE_HOLD - Release an amount from an accountId.
     *         GET_AVAILABLE - Get available balance from the accountId.
     *         GET_RESERVED - Get the reserved funds from the accountId.
     *         REGISTER_CLIENT - Register a client account.
     *         REGISTER_AH - Register an AuctionHouse account.
     *         GET_NET_INFO - Get the AuctionHouse net information.
     */
    public enum Command {
        DEPOSIT,
        TRANSFER,
        HOLD,
        RELEASE_HOLD,
        GET_AVAILABLE,
        GET_RESERVED,
        REGISTER_CLIENT,
        REGISTER_AH,
        DEREGISTER_AH,
        DEREGISTER_CLIENT,
        GET_NET_INFO,
    }

    /**
     * Inner Builder class implementing the builder pattern.
     */
    public static class Builder {
        private Double amount = null;
        private UUID sender = null;
        private UUID accountId = null;
        private Command command = null;
        private Response response = null;
        private List<NetInfo> netInfo = null;

        /**
         * Assign the NetInfo to this message.
         * @param netInfo List<NetInfo></NetInfo>
         * @return Builder
         */
        public Builder netInfo(List<NetInfo> netInfo) {
            this.netInfo = netInfo;
            return this;
        }

        /**
         * Assign the command to this Message.
         * @param command Enum
         * @return Builder
         */
        public Builder command(Command command) {
            this.command = command;
            return this;
        }

        /**
         * Assign the amount to this Message.
         * @param amount Double
         * @return Builder
         */
        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Assign the UUID to this Message.
         * @param accountId UUID
         * @return Builder
         */
        public Builder accountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        /**
         * Assign the response to this Message.
         * @param response enum
         * @return Builder
         */
        public Builder response(Response response) {
            this.response = response;
            return this;
        }

        /**
         * Assign the sender UUID to this message and return a Message object.
         * @param sender UUID
         * @return Message
         */
        public Message send(UUID sender) {
            this.sender = sender;
            return new Message(this);
        }
    }
}