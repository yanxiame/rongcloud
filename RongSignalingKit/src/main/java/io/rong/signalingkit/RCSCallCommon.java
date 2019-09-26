package io.rong.signalingkit;

/**
 * Created by weiqinxiao on 16/3/1.
 */
public class RCSCallCommon {

    public enum CallMediaType {
        AUDIO(1),
        VIDEO(2);

        private int value;

        CallMediaType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallMediaType valueOf(int value) {
            for (CallMediaType v : CallMediaType.values()) {
                if (v.value == value)
                    return v;

            }
            return null;
        }
    }

    public enum CallUserType {
        NORMAL(1),
        OBSERVER(2);

        private int value;

        CallUserType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallUserType valueOf(int value) {
            for (CallUserType v : CallUserType.values()) {
                if (v.value == value)
                    return v;

            }
            return null;
        }
    }

    public enum CallErrorCode {
        /**
         * 开通的音视频服务没有及时生效或音视频服务已关闭，请等待3-5小时后重新安装应用或开启音视频服务再进行测试
         */
        ENGINE_NOT_FOUND(1);

        private int value;

        CallErrorCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallErrorCode valueOf(int value) {
            for (CallErrorCode v : CallErrorCode.values()) {
                if (v.value == value)
                    return v;
            }
            return null;
        }

    }

    public enum CallDisconnectedReason {
        /**
         * 己方取消已发出的通话请求
         */
        CANCEL(1),

        /**
         * 己方拒绝收到的通话请求
         */
        REJECT(2),

        /**
         * 己方挂断
         */
        HANGUP(3),

        /**
         * 己方忙碌
         */
        BUSY_LINE(4),

        /**
         * 己方未接听
         */
        NO_RESPONSE(5),

        /**
         * 当前引擎不支持
         */
        ENGINE_UNSUPPORTED(6),


        /**
         * 己方网络出错
         */
        NETWORK_ERROR(7),


        /**
         * 己方摄像头初始化错误，可能是没有打开使用摄像头权限
         */
        INIT_VIDEO_ERROR(8),

        /**
         * 其他端已经接听
         */
        OTHER_DEVICE_HAD_ACCEPTED(9),

        /**
         * 对方取消已发出的通话请求
         */
        REMOTE_CANCEL(11),

        /**
         * 对方拒绝收到的通话请求
         */
        REMOTE_REJECT(12),

        /**
         * 通话过程对方挂断
         */
        REMOTE_HANGUP(13),

        /**
         * 对方忙碌
         */
        REMOTE_BUSY_LINE(14),

        /**
         * 对方未接听
         */
        REMOTE_NO_RESPONSE(15),

        /**
         * 对方引擎不支持
         */
        REMOTE_ENGINE_UNSUPPORTED(16),

        /**
         * 对方网络错误
         */
        REMOTE_NETWORK_ERROR(17),

        /**
         * im ipc服务已断开
         */
        SERVICE_DISCONNECTED(18),
        /**
         * 对方被加入黑名单
         */
        REMOTE_BLOCKED(19);

        /**
         * 用户被开发者后台封禁，该用户会被踢出音视频房间
         */
//        CONN_USER_BLOCKED(19)

        private int value;

        CallDisconnectedReason(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallDisconnectedReason valueOf(int value) {
            for (CallDisconnectedReason v : CallDisconnectedReason.values()) {
                if (v.value == value)
                    return v;
            }
            return null;
        }
    }

    public enum CallModifyMemType {
        MODIFY_MEM_TYPE_ADD(1),
        MODIFY_MEM_TYPE_REMOVE(2);

        private int value;

        CallModifyMemType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallModifyMemType valueOf(int value) {
            for (CallModifyMemType v : CallModifyMemType.values()) {
                if (v.value == value)
                    return v;

            }
            return null;
        }
    }

    public enum CallEngineType {
        ENGINE_TYPE_AGORA(1),
        ENGINE_TYPE_RONG(2),
        ENGINE_TYPE_BLINK(3),
        ENGINE_TYPE_RTC(4);

        private int value;

        CallEngineType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallEngineType valueOf(int value) {
            for (CallEngineType v : CallEngineType.values()) {
                if (v.value == value)
                    return v;

            }
            return null;
        }
    }

    public enum CallStatus {
        IDLE(0),
        OUTGOING(1),
        INCOMING(2),
        CONNECTING(3),
        CONNECTED(4);

        private int value;

        CallStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static CallStatus valueOf(int value) {
            for (CallStatus v : CallStatus.values()) {
                if (v.value == value)
                    return v;
            }
            return null;
        }
    }

    public enum CallPermission {
        PERMISSION_AUDIO,
        PERMISSION_CAMERA,
        PERMISSION_AUDIO_AND_CAMERA
    }

    public enum CallVideoProfile {
        /**
         * 分辨率:144x176,  帧率:15
         */
        VD_144x176_15f,
        /**
         * 分辨率:144x176,  帧率:24
         */
        VD_144x176_24f,
        VD_144x176_30f,

        VD_144x256_15f,
        VD_144x256_24f,
        VD_144x256_30f,

        VD_240x240_15f,
        VD_240x240_24f,
        VD_240x240_30f,

        VD_240x320_15f,
        VD_240x320_24f,
        VD_240x320_30f,

        VD_360x480_15f,
        VD_360x480_24f,
        VD_360x480_30f,

        VD_360x640_15f,
        VD_360x640_24f,
        VD_360x640_30f,

        VD_368x640_15f,
        VD_368x640_24f,
        VD_368x640_30f,

        VD_480x640_15f,
        VD_480x640_24f,
        VD_480x640_30f,

        VD_480x720_15f,
        VD_480x720_24f,
        VD_480x720_30f,

        VD_480x854_15f,
        VD_480x854_24f,
        VD_480x854_30f,

        VD_720x1280_15f,
        VD_720x1280_24f,
        VD_720x1280_30f,

        VD_1080x1920_15f,
        VD_1080x1920_24f,
        VD_1080x1920_30f;

        CallVideoProfile() {
        }

        public static CallVideoProfile getCallVideoProfile(String value) {
            for (CallVideoProfile v : CallVideoProfile.values()) {
                if (v.name().equals(value))
                    return v;
            }
            return null;
        }
    }

    public enum ServerRecordingErrorCode {
        /**
         * 操作成功
         */
        SUCCESS(0),

        /**
         * 执行请求失败
         */
        FAIL(1),

        /**
         * 参数无效
         */
        INVALID_ARGUMENT(2),

        /**
         * 未就绪
         */
        NOT_READY(3),

        /**
         * 不在通话中
         */
        NOT_IN_CALL(4),

        /**
         * 录音服务器 IP 未初始化
         */
        NOT_INITIALIZED(7),

        /**
         * 操作超时
         */
        TIME_OUT(10);

        private int value;

        ServerRecordingErrorCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ServerRecordingErrorCode valueOf(int value) {
            for (ServerRecordingErrorCode v : ServerRecordingErrorCode.values()) {
                if (v.value == value)
                    return v;
            }
            return null;
        }
    }
}
