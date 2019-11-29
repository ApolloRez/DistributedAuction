package bankservice.message;

import shared.NetInfo;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Message implements Serializable {

    private final Double amount;
    private final UUID sender;
    private final UUID accountId;
    private final Command command;
    private final Response response;
    private final List<NetInfo> netInfo;

    public Message(Builder builder) {
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
                .command(Command.HOLD)
                .send(UUID.randomUUID());
        System.out.println(message.getAccountId());
        System.out.println(message.getAmount());
    }

    public Response getResponse() {
        return response;
    }

    public Double getAmount() {
        return amount;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Command getCommand() {
        return command;
    }

    public List<NetInfo> getNetInfo() {
        return netInfo;
    }

    public enum Command {
        DEPOSIT,
        TRANSFER,
        HOLD,
        GET_AVAILABLE,
        GET_RESERVED,
        REGISTER_CLIENT,
        REGISTER_AH,
        GET_NETINFO,
    }

    public enum Response {
        SUCCESS,
        ERROR,
        INSUFFICIENT_FUNDS,
    }

    public static class Builder {
        private Double amount = null;
        private UUID sender = null;
        private UUID accountId = null;
        private Command command = null;
        private Response response = null;
        private List<NetInfo> netInfo = null;

        public Builder netInfo(List<NetInfo> netInfo) {
            this.netInfo = netInfo;
            return this;
        }

        public Builder command(Command command) {
            this.command = command;
            return this;
        }

        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public Builder accountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder response(Response response) {
            this.response = response;
            return this;
        }

        public Message send(UUID sender) {
            this.sender = sender;
            return new Message(this);
        }
    }
}
