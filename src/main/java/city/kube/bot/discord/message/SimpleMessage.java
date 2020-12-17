package city.kube.bot.discord.message;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class SimpleMessage extends DiscordMessage {
    private final Message message;

    public SimpleMessage(TextChannel channel, Message message) {
        super(channel);
        this.message = message;
    }

    @Override
    public void send() {
        getChannel().sendMessage(message).complete();
    }

}
