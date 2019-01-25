package com.roje.bombak.gate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pc
 */
@ConfigurationProperties(prefix = "gate")
public class GateProperties {

    private int loginTimeout = 30;

    private Netty netty = new Netty();

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public Netty getNetty() {
        return netty;
    }

    public void setNetty(Netty netty) {
        this.netty = netty;
    }

    public static class Netty {
        private int readerIdleTimeSeconds = 0;

        private int writerIdleTimeSeconds = 0;

        private int allIdleTimeSeconds = 0;

        private int executorThreadPoolSize = 3;

        private int port = 4001;

        public int getReaderIdleTimeSeconds() {
            return readerIdleTimeSeconds;
        }

        public void setReaderIdleTimeSeconds(int readerIdleTimeSeconds) {
            if (readerIdleTimeSeconds > 0) {
                this.readerIdleTimeSeconds = readerIdleTimeSeconds;
            }
        }

        public int getWriterIdleTimeSeconds() {
            return writerIdleTimeSeconds;
        }

        public void setWriterIdleTimeSeconds(int writerIdleTimeSeconds) {
            if (writerIdleTimeSeconds > 0) {
                this.writerIdleTimeSeconds = writerIdleTimeSeconds;
            }
        }

        public int getAllIdleTimeSeconds() {
            return allIdleTimeSeconds;
        }

        public void setAllIdleTimeSeconds(int allIdleTimeSeconds) {
            if (allIdleTimeSeconds > 0) {
                this.allIdleTimeSeconds = allIdleTimeSeconds;
            }
        }

        public int getExecutorThreadPoolSize() {
            return executorThreadPoolSize;
        }

        public void setExecutorThreadPoolSize(int executorThreadPoolSize) {
            if (executorThreadPoolSize > 0) {
                this.executorThreadPoolSize = executorThreadPoolSize;
            }
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            if (port > 0) {
                this.port = port;
            }
        }
    }
}
