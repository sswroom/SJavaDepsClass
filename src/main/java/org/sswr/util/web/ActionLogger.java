package org.sswr.util.web;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sswr.util.basic.ThreadVar;
import org.sswr.util.db.DBUpdateHandler;
import org.sswr.util.db.DBUtil;
import org.sswr.util.io.ActionFileStore;

@Component
public class ActionLogger implements DBUpdateHandler
{
    @Autowired
    private ActionFileStore store;

	public String getUser()
	{
		return (String)ThreadVar.get("User");
	}

    public ActionLogger()
    {
        DBUtil.setUpdateHandler(this);
    }

    @PostLoad
    public void onPostLoad(AuditModel model)
    {
        model.updateCurrVal();
    }

    @PostPersist
    public void onPostPersist(AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.CREATE, model.getLastValue(), model.toString());
        model.updateCurrVal();
    }

    @PostUpdate
    public void onPostUpdate(AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.UPDATE, model.getLastValue(), model.toString());
        model.updateCurrVal();
    }

    @PostRemove
    public void onPostRemove(AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.DELETE, model.getLastValue(), model.toString());
    }

    @Override
    public void dbUpdated(Object oldObj, Object newObj) {
        if (oldObj == null && newObj == null)
        {

        }
        else if (oldObj == null)
        {
            store.logAction(3, getUser(), ActionFileStore.ActionType.CREATE, null, newObj.toString());
        }
        else if (newObj == null)
        {
            store.logAction(3, getUser(), ActionFileStore.ActionType.DELETE, oldObj.toString(), null);
        }
        else
        {
            store.logAction(3, getUser(), ActionFileStore.ActionType.UPDATE, oldObj.toString(), newObj.toString());
        }
    }
}
