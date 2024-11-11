package db.entity;

public class Gift {
    private int gift_id;
    private String telegramTag;
    private String name;
    private String link;

    public Gift(int gift_id, String telegramTag, String name, String link) {
        this.gift_id = gift_id;
        this.telegramTag = telegramTag;
        this.name = name;
        this.link = link;
    }

    public int getGift_id() {
        return gift_id;
    }

    public String getTelegramTag() {
        return telegramTag;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }
}
