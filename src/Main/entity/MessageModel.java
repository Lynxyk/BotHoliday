package Main.entity;

public class MessageModel {
    private String name;
    private String link;

    public MessageModel(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }
}
