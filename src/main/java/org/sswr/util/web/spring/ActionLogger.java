package org.sswr.util.web.spring;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sswr.util.basic.ThreadVar;
import org.sswr.util.db.DBUpdateHandler;
import org.sswr.util.db.DBUtil;
import org.sswr.util.io.ActionFileStore;
import org.sswr.util.web.AuditModel;

@Component
public class ActionLogger implements DBUpdateHandler
{
    @Autowired
    private ActionFileStore store;

    @Nullable
	public String getUser()
	{
		return (String)ThreadVar.get("User");
	}

    public ActionLogger()
    {
        DBUtil.addUpdateHandler(this);
    }

    @PostLoad
    public void onPostLoad(@Nonnull AuditModel model)
    {
        model.updateCurrVal();
    }

    @PostPersist
    public void onPostPersist(@Nonnull AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.CREATE, model.getLastValue(), model.toString());
        model.updateCurrVal();
    }

    @PostUpdate
    public void onPostUpdate(@Nonnull AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.UPDATE, model.getLastValue(), model.toString());
        model.updateCurrVal();
    }

    @PostRemove
    public void onPostRemove(@Nonnull AuditModel model)
    {
        store.logAction(4, getUser(), ActionFileStore.ActionType.DELETE, model.getLastValue(), model.toString());
    }

    @Override
    public void dbUpdated(@Nullable Object oldObj, @Nullable Object newObj) {
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
