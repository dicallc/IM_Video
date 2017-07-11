package com.xiaoxin.im.model;

import java.util.List;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public class AppStoreModel {

        public AppStoreModel(String mCode) {
                code = mCode;
        }

        public AppStoreModel() {
        }

        /**
         * code : 200
         * msg : success
         * content : [{"title":"开眼","down_path":"AdvertCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/kaiyan.jpg"},{"title":"TODO","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/todo.jpg"},{"title":"一个","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/one.png"},{"title":"钛媒体","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/%E9%92%9B%E5%AA%92%E4%BD%93.png"},{"title":"36氪","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/36%E6%B0%AA.jpg"},{"title":"少数派","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/%E5%B0%91%E6%95%B0%E6%B4%BE.jpg"},{"title":"下厨房","down_path":"EntryListCard","img":"http://7xnk07.com1.z0.glb.clouddn.com/%E4%B8%8B%E5%8E%A8%E6%88%BF.png"}]
         */

        public String code;
        public String msg;
        public List<ContentBean> content;

        public static class ContentBean {
                public ContentBean() {
                }

                public ContentBean(String mTitle, String mDown_path, String mImg,
                    String mVersion_des, String mApp_type,String is) {
                        title = mTitle;
                        down_path = mDown_path;
                        img = mImg;
                        version_des = mVersion_des;
                        app_type = mApp_type;
                        isCustom = is;
                }


                /**
                 * title : 开眼
                 * down_path : AdvertCard
                 * img : http://7xnk07.com1.z0.glb.clouddn.com/kaiyan.jpg
                 */

                public String title;
                public String down_path;
                public String img;
                public String version;
                public String version_des;
                public String down_size;
                public String app_type;
                public String isCustom;
        }
}
