package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import nlogger.nlogger;
import session.session;
import string.StringHelper;

public class Menu {
	private GrapeTreeDBModel menu;
	private GrapeDBSpecField gDbSpecField;
	private session se;
	private JSONObject userInfo = null;
	private String userUgid = null;

	public Menu() {

		menu = new GrapeTreeDBModel();
		gDbSpecField = new GrapeDBSpecField();
		gDbSpecField.importDescription(appsProxy.tableConfig("menu"));
		menu.descriptionModel(gDbSpecField);
		menu.bindApp();

		se = new session();
		userInfo = se.getDatas();
		if (userInfo != null && userInfo.size() != 0) {
			userUgid = userInfo.getString("ugid");   //角色id
		}  
	}

	/**
	 * 新增菜单
	 * 
	 * @project GrapeMenu
	 * @package interfaceApplication
	 * @file Menu.java
	 * 
	 * @param mString
	 *            待操作数据
	 * @return {"message":"新增菜单成功","errorcode":0} 或
	 *         {"message":"其他异常","errorcode":99}
	 *
	 */
	public String AddMenu(String mString) {
		JSONObject object = null;
		String result = rMsg.netMSG(100, "新增失败");
		try {
			if (StringHelper.InvaildString(mString)) {
				return rMsg.netMSG(1, "参数错误");
			}
			object = JSONObject.toJSON(mString);
			object = Add(object);
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		result = (object != null && object.size() > 0) ? rMsg.netMSG(0, "新增成功", object) : result;
		return result;
	}

	/**
	 * 新增操作
	 * 
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONObject Add(JSONObject object) {
		String name = "", id = "", prvid = "";
		JSONObject menuObj = null;
		if (object != null && object.size() > 0) {
			if (object.containsKey("name")) {
				name = object.getString("name");
			}
			JSONObject temp = findByName(name);
			if (temp != null && temp.size() > 0) {
				menuObj = findMenu(name, userUgid);
				if (menuObj == null || menuObj.size() <= 0) {
					id = temp.getMongoID("_id");
					prvid = temp.getString("prvid");
					prvid = prvid + "," + userUgid;
					object.put("prvid", prvid);
					menu.eq("_id", id).data(object).update();
				}
			} else {
				object.put("prvid", userUgid);
				id = (String) menu.data(object).insertOnce();
			}
		}
		return find(id);
	}

	/**
	 * 根据菜单名称和角色id验证菜单是否已存在
	 * 
	 * @param menuName
	 * @param ugid
	 * @return
	 */
	private JSONObject findByName(String menuName) {
		JSONObject object = null;
		object = menu.eq("name", menuName).find();
		return object;
	}

	/**
	 * 根据菜单名称和角色id验证菜单是否已存在
	 * 
	 * @param menuName
	 * @param ugid
	 * @return
	 */
	private JSONObject findMenu(String menuName, String ugid) {
		JSONObject object = null;
		object = menu.eq("name", menuName).like("prvid", ugid).find();
		return object;
	}

	/**
	 * 根据菜单名称和角色id验证菜单是否已存在
	 */
	private JSONObject find(String mid) {
		JSONObject object = null;
		if (!StringHelper.InvaildString(mid) && ObjectId.isValid(mid)) {
			object = menu.eq("_id", mid).find();
		}
		return object;
	}

	/**
	 * 修改菜单
	 * 
	 * @project GrapeMenu
	 * @package interfaceApplication
	 * @file Menu.java
	 * 
	 * @param mString
	 *            待操作数据
	 * @return {"message":"新增菜单成功","errorcode":0} 或
	 *         {"message":"其他异常","errorcode":99}
	 *
	 */
	public String UpdateMenu(String mid, String mString) {
		Object tip = null;
		String result = rMsg.netMSG(100, "修改失败");
		try {
			if (StringHelper.InvaildString(mid) || StringHelper.InvaildString(mString)) {
				return rMsg.netMSG(1, "参数错误");
			}
			JSONObject object = JSONObject.toJSON(mString);
			tip = menu.eq("_id", mid).data(object).update();
		} catch (Exception e) {
			nlogger.logout(e);
			tip = null;
		}
		return (tip != null) ? rMsg.netMSG(0, "修改成功") : result;
	}

	/**
	 * 删除菜单
	 * 
	 * @param id
	 * @return
	 */
	public String DeleteMenu(String id) {
		return DeleteBatchMenu(id);
	}

	public String DeleteBatchMenu(String id) {
		String[] value = null;
		long code = 0;
		String result = rMsg.netMSG(100, "删除失败");
		try {
			if (StringHelper.InvaildString(id)) {
				return rMsg.netMSG(1, "参数错误");
			}
			value = id.split(",");
			if (value != null) {
				menu.or();
				for (String mid : value) {
					if (ObjectId.isValid(mid)) {
						menu.eq("_id", mid);
					}
				}
				code = menu.deleteAll();
			}
		} catch (Exception e) {
			nlogger.logout(e);
			code = 0;
		}
		result = code > 0 ? rMsg.netMSG(0, "删除成功") : result;
		return result;
	}

	/**
	 * 根据当前用户的角色显示菜单信息
	 * 
	 * @return
	 */
	public String ShowMenu() {
		JSONArray array = null;
		if (StringHelper.InvaildString(userUgid)) {
			array = menu.eq("state", 0).like("prvid", userUgid).select();
		}
		return rMsg.netMSG(true, (array != null && array.size() > 0) ? array : new JSONArray());
	}
}
