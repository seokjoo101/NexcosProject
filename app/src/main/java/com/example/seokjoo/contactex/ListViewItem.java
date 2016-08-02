package com.example.seokjoo.contactex;

import android.graphics.drawable.Drawable;

/**
 * Created by Seokjoo on 2016-08-01.
 */
public class ListViewItem {

        private Drawable manDrawable ;
        private Drawable callDrawable ;
        private String titleStr ;
        private String descStr ;

        public void setManDrawable(Drawable icon) {
            manDrawable = icon ;
        } public void setCallDrawable(Drawable icon) {
            callDrawable = icon ;
        }
        public void setTitle(String title) {
            titleStr = title ;
        }
        public void setDesc(String desc) {
            descStr = desc ;
        }

        public Drawable getManDrawable() {
            return this.manDrawable ;
        } public Drawable getCallDrawable() {
            return this.callDrawable ;
        }
        public String getTitle() {
            return this.titleStr ;
        }
        public String getDesc() {
            return this.descStr ;
        }

}
