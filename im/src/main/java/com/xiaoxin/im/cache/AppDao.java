package com.xiaoxin.im.cache;

import android.text.TextUtils;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.CustomAppModel;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class AppDao {
  public static void deleteCustomApp(final String name,final onConnectionFinishLinstener mLinstener) {
    if (TextUtils.isEmpty(name)) return;
    final Realm mRealm = Realm.getDefaultInstance();
    mRealm.executeTransactionAsync(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        RealmQuery<CustomAppModel> query = realm.where(CustomAppModel.class);
        query.equalTo("mApp_name", name);
        RealmResults<CustomAppModel> result1 = query.findAll();
        if (result1.size() > 0) {
          result1.get(0).deleteFromRealm();
          mLinstener.onSuccess(110, "");
        }else{
          mLinstener.onFail(404, "删除失败，数据库无此数据");
        }
      }
    });
  }
}
