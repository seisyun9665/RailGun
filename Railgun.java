package seisyun.minecraft.plugin.railgun.test.railgun;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;


public final class Railgun extends JavaPlugin implements Listener {

    /* 初期変数宣言 */

    // レールガンとなる弓の名前
    private static final String BOW_NAME = "超電磁砲";

    // 発射した人からどれくらい離れたらレールガンが爆発し始めるか
    private static final double BLAST_DISTANCE = 7;

    // 発射してから何tickまで有効か
    private static final int VALID_TIME = 40;

    /* プラグインが起動した時の処理 */
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("RailGun Test Enabled.");

        // イベント登録（弓を使った時の処理）
        getServer().getPluginManager().registerEvents(this, this);
    }

    /* プラグインが終了した時の処理 */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("RailGun Test Disabled.");
    }


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        // railgun get コマンドが実行された場合
        if(args[0].equalsIgnoreCase("get")) {
            // コマンド実行者がプレイヤーの場合
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // アイテムを設定
                ItemStack bow = getItem(Material.BOW, BOW_NAME, null);

                // プレイヤーのインベントリにアイテムを追加
                player.getInventory().addItem(bow);

                // 正常終了
                return true;
            }
        }
        return false;
    }

    /* 指定した種類・名前のアイテム１個を返す関数 */
    private ItemStack getItem(Material material, String name, List<String> lore){
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if(lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    /* エンティティが弓を使った時 */
    @EventHandler
    public void onPlayerShoot(EntityShootBowEvent e){

        /* 弓の使用者がプレイヤー　かつ　発射された飛翔体が矢の時 */
        if(e.getEntity() instanceof Player && e.getProjectile() instanceof Arrow){

            /* プレイヤーが持つ弓の名前を判定する処理 */
            // プレイヤーが所持しているアイテムを取得
            ItemStack item = e.getBow();

            // アイテムがアイテムメタ情報を持っている時
            if(item.hasItemMeta()){
                ItemMeta meta = item.getItemMeta();

                // アイテムメタ情報の中にDisplayNameのデータがある時
                if(meta.hasDisplayName()) {
                    String name = meta.getDisplayName();

                    // DisplayNameが超電磁砲の時
                    if (name.equals(BOW_NAME)) {

                        /* 変数宣言 */
                        Player player = (Player) e.getEntity();
                        Arrow arrow = (Arrow) e.getProjectile();

                        /* 最大までためてから発射した時 */
                        if (arrow.isCritical()) {

                            /* レールガン処理 */

                            // 矢の重力をなくす
                            arrow.setGravity(false);

                            // プレイヤーの視線の向きを取得
                            final Vector direction_player = player.getLocation().getDirection();

                            // 矢の向いている方向をプレイヤーの視線の向きと一致させる
                            arrow.getLocation().setDirection(direction_player);

                            for (int i = 0; i < VALID_TIME; i++) {
                                getServer().getScheduler().runTaskLater(this, new Runnable() {
                                    @Override
                                    public void run() {
                                        Location loc_arrow = arrow.getLocation();
                                        Location loc_player = player.getLocation();
                                        if (loc_arrow.distance(loc_player) > BLAST_DISTANCE) {
                                            // 矢から2ブロック後方の位置を取得
                                            Location loc_tnt = loc_arrow.add(direction_player.clone().multiply(-2));

                                            // tntを生成
                                            TNTPrimed tnt = (TNTPrimed) loc_tnt.getWorld().spawnEntity(loc_tnt, EntityType.PRIMED_TNT);

                                            // tntの爆発時間を0に設定
                                            tnt.setFuseTicks(0);

                                            // パーティクル演出
                                            loc_arrow.getWorld().spawnParticle(Particle.FLAME, loc_arrow, 100, 0, 0, 0, 0.5);
                                            loc_arrow.getWorld().spawnParticle(Particle.END_ROD, loc_arrow, 100, 0, 0, 0, 0.3);
                                            loc_arrow.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc_arrow, 100, 0, 0, 0, 0.7);
                                        }
                                    }
                                }, i);
                            }

                            // 時間経過した矢を消す処理
                            getServer().getScheduler().runTaskLater(this, new Runnable() {
                                @Override
                                public void run() {
                                    arrow.remove();
                                }
                            }, VALID_TIME);
                        }
                    }
                }
            }

        }
    }

}
