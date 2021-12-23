/*
package com.arrow.pay.common;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

*/
/**
 * 自定义ID生成器
 *
 * @author Greenarrow
 *//*

@Slf4j
@Component
public class CustomIdGenerator implements IdentifierGenerator {


    private static final int TOTAL_BITS = 64;
    private static final int EPOCH_BITS = 42;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final int MAXNODEID = (int) (Math.pow(2, NODE_ID_BITS) - 1);
    private static final int MAXSEQUENCE = (int) (Math.pow(2, SEQUENCE_BITS) - 1);
    */
/**
     * 自定义Epoch，格林威治时间为 2015-01-01 00:00:00 北京时间为 2015-01-01 08:00:00
     *//*

    private static final long CUSTOM_EPOCH = 1420070400000L;

    private volatile long sequence = 0L;
    private volatile long lastTimestamp = -1L;

    private final int globalNodeId;
    private final AtomicLong al = new AtomicLong(1);

    public CustomIdGenerator() {
        this.globalNodeId = createNodeId();
    }
    @Override
    public Long nextId(Object entity) {
        long currentTimestamp = timestamp();
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("无效的系统时钟！");
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAXSEQUENCE;

            // 代表序列已用尽，等待下一个毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (TOTAL_BITS - EPOCH_BITS);
        id |= (globalNodeId << (TOTAL_BITS - EPOCH_BITS - NODE_ID_BITS));
        id |= sequence;
        return id;
    }

    private static long timestamp() {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    */
/**
     * 等待到下一个毫秒
     *
     * @param currentTimestamp 当前时间戳
     * @return
     *//*

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }


    */
/**
     * 创建集群节点编号
     *
     * @return
     *//*

    private int createNodeId() {
        int nodeId;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] macAddress = networkInterface.getHardwareAddress();
                if (macAddress != null) {
                    for (int i = 0; i < macAddress.length; i++) {
                        stringBuilder.append(String.format("%02X", macAddress[i]));
                    }
                }
            }
            nodeId = stringBuilder.toString().hashCode();
        } catch (Exception e) {
            nodeId = (new SecureRandom().nextInt());
        }
        nodeId = nodeId & MAXNODEID;
        return nodeId;
    }

}
*/
