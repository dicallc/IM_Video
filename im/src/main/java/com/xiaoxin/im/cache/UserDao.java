package com.xiaoxin.im.cache;

import android.text.TextUtils;
import com.xiaoxin.im.model.UserEntry;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class UserDao {
  public static void saveUserEntry(final String name) {
    if (TextUtils.isEmpty(name)) return;
    Realm mRealm = Realm.getDefaultInstance();
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        RealmQuery<UserEntry> query = realm.where(UserEntry.class);
        query.equalTo("nick_name", name);
        RealmResults<UserEntry> mUserEntries = query.findAll();
        //如果已经存在就修改
        if (mUserEntries.size() > 0) {
          mUserEntries.get(0).nick_name = name;
        } else {
          //不存在就添加
          UserEntry mEntry = realm.createObject(UserEntry.class);
          mEntry.nick_name = name;
        }
      }
    });
  }

  public static String getUserEntry() {
    Realm mRealm = Realm.getDefaultInstance();
    UserEntry mEntry = mRealm.where(UserEntry.class).findFirst();
    return mEntry.nick_name;
  }
}
