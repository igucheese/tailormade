package com.tailormade.tailor.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // 設定項目の定義
    public static final ModConfigSpec.ConfigValue<Integer> INITIAL_TELEPORT_X;
    public static final ModConfigSpec.ConfigValue<Integer> INITIAL_TELEPORT_Y;
    public static final ModConfigSpec.ConfigValue<Integer> INITIAL_TELEPORT_Z;
    public static final ModConfigSpec.ConfigValue<Integer> INITIAL_WRIT_COUNT;
    // 権利書
    public static final ModConfigSpec.ConfigValue<String> PURCHASE_CURRENCY;
    public static final ModConfigSpec.ConfigValue<Integer> WRIT_BASE_COST;
    // 土地編集権限
    public static final ModConfigSpec.ConfigValue<Integer> LAND_EDIT_PERMISSION_LEVEL;
    // スプシ使用
    public static final ModConfigSpec.ConfigValue<String> SPREADSHEET_URL;
    public static final ModConfigSpec.ConfigValue<Boolean> USE_SPREADSHEET;
    // Discord通知
    public static final ModConfigSpec.ConfigValue<Boolean> PURCHASE_NOTIFY_DISCORD;
    public static final ModConfigSpec.ConfigValue<String> PURCHASE_DISCORD_WEBHOOK_URL;
    // ポールを引っ張れる距離
    public static final ModConfigSpec.ConfigValue<Integer> LANDPOLE_DISTANCE;

    // モクパケ配送
    public static final ModConfigSpec.ConfigValue<String> POST_COST_ITEM_NORMAL;
    public static final ModConfigSpec.ConfigValue<String> POST_COST_ITEM_EXPRESS;
    public static final ModConfigSpec.ConfigValue<Integer> POST_COST_AMOUNT_NORMAL;
    public static final ModConfigSpec.ConfigValue<Integer> POST_COST_AMOUNT_EXPRESS;
    public static final ModConfigSpec.ConfigValue<Integer> DELIVERY_DURATION_NORMAL;
    public static final ModConfigSpec.ConfigValue<Integer> DELIVERY_DURATION_EXPRESS;


    static {
        BUILDER.push("MEZCraft Server Config");

        INITIAL_TELEPORT_X = BUILDER
                .comment("「冒険を始める」ボタンをクリックしたときに移動する座標（X）です")
                .define("initial_teleport_x", 100);

        INITIAL_TELEPORT_Y = BUILDER
                .comment("「冒険を始める」ボタンをクリックしたときに移動する座標（Y）です")
                .define("initial_teleport_y", 64);

        INITIAL_TELEPORT_Z = BUILDER
                .comment("「冒険を始める」ボタンをクリックしたときに移動する座標（Z）です")
                .define("initial_teleport_z", 100);

        INITIAL_WRIT_COUNT = BUILDER
                .comment("「冒険を始める」ボタンをクリックしたときにもらえる権利書の数です")
                .define("initial_writ_count", 1);

        PURCHASE_CURRENCY = BUILDER
                .comment("権利書の購入に使えるアイテムです")
                .define("purchase_currency", "minecraft:emerald");

        WRIT_BASE_COST = BUILDER
                .comment("権利書の初期購入価格です")
                .define("writ_base_cost", 4);

        LANDPOLE_DISTANCE = BUILDER
                .comment("ポール設置時にラインを描けるブロック数です")
                .define("landpole_distance", 32);

        LAND_EDIT_PERMISSION_LEVEL = BUILDER
                .comment("他人の土地を編集できる管理者権限レベルです")
                .define("land_edit_permission_level", 3);

        SPREADSHEET_URL = BUILDER
                .comment("Google Apps Script (GAS) のデプロイURLを入力してください。")
                .define("spreadsheetUrl", "https://script.google.com/macros/s/xxxx/exec");

        USE_SPREADSHEET = BUILDER
                .comment("不動産管理にスプレッドシートを使うかどうかを設定してください。")
                .define("useSpreadsheet", false);

        PURCHASE_NOTIFY_DISCORD = BUILDER
                .comment("プレイヤーが土地を購入したとき、Discord に通知するかどうかを設定してください。")
                .define("purchase_notify_discord", false);

        PURCHASE_DISCORD_WEBHOOK_URL = BUILDER
                .comment("通知したいチャンネルの Webhook URL を入力してください。")
                .define("purchase_discord_webhook_url", "");

        POST_COST_ITEM_NORMAL = BUILDER
                .comment("モクパケを普通送付する際に必要なアイテムです")
                .define("post_cost_item_normal", "minecraft:emerald");

        POST_COST_AMOUNT_NORMAL = BUILDER
                .comment("モクパケを普通送付する際に必要なアイテムの数量です")
                .define("post_cost_amount_normal", 0);

        POST_COST_ITEM_EXPRESS = BUILDER
                .comment("モクパケを速達送付する際に必要なアイテムです")
                .define("post_cost_item_express", "minecraft:emerald");

        POST_COST_AMOUNT_EXPRESS = BUILDER
                .comment("モクパケを速達送付する際に必要なアイテムの数量です")
                .define("post_cost_amount_express", 1);

        DELIVERY_DURATION_NORMAL = BUILDER
                .comment("モクパケ普通配送時の所要時間（秒）です。")
                .define("delivery_duration_normal", 600);

        DELIVERY_DURATION_EXPRESS = BUILDER
                .comment("モクパケ速達配送時の所要時間（秒）です。")
                .define("delivery_duration_express", 30);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
