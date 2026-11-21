/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.bukkit;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.bridge.DynmapBridge;
import com.github.ucchyocean.lc3.channel.JapanizeConvertTask;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberPlayer;

/**
 * Japanize2行表示のときに、変換結果を遅延して通常チャットに表示するためのタスク
 * @author ucchy
 */
public class BukkitNormalChatJapanizeTask implements Runnable {

    private ChannelMember player;
    private final List<Player> recipients;

    private JapanizeConvertTask task;

    /**
     * コンストラクタ
     * @param org 変換前の文字列
     * @param type 変換タイプ
     * @param player 発言したプレイヤー
     * @param japanizeFormat 変換後に発言するときの、発言フォーマット
     * @param recipients メッセージ受信者
     */
    public BukkitNormalChatJapanizeTask(String org, JapanizeType type,
            ChannelMember player, String japanizeFormat, Collection<Player> recipients) {

        task = new JapanizeConvertTask(org, type, japanizeFormat, null, player);
        this.player = player;
        this.recipients = List.copyOf(recipients);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if ( task.runSync() ) {

            String result = task.getResult();

            LunaChatBukkit plugin = LunaChatBukkit.getInstance();
            // 送信
            for ( Player p : recipients ) {
                plugin.runForPlayer(p, () -> p.sendMessage(result));
            }
            Bukkit.getConsoleSender().sendMessage(result);

            // 設定に応じてdynmapへ送信する
            DynmapBridge dynmap = plugin.getDynmap();
            if ( LunaChat.getConfig().isSendBroadcastChannelChatToDynmap() && dynmap != null ) {
                Player source = player instanceof ChannelMemberPlayer member ? member.getPlayer() : null;
                if ( source != null ) {
                    plugin.runForPlayer(source, () -> dynmap.chat(source, result));
                } else {
                    dynmap.broadcast(result);
                }
            }
        }
    }
}
