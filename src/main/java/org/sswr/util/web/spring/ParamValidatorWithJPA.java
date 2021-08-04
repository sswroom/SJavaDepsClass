package org.sswr.util.web.spring;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.web.ParamValidator;

public class ParamValidatorWithJPA extends ParamValidator
{
	public ParamValidatorWithJPA(String funcName, LogTool logger, HttpServletRequest req, HttpServletResponse resp)
	{
		super(funcName, logger, req, resp);
	}

	public <T> boolean checkEntityExists(JpaRepository<T, Integer> repo, String varName, String varValue, Enum<?> checkStatus, boolean invalidCheck)
	{
		if (this.errMsg != null) return true;
		String findFunc = "findBy"+Character.toUpperCase(varName.charAt(0))+varName.substring(1);
		try
		{
			Method findMeth = repo.getClass().getMethod(findFunc, new Class<?>[]{String.class});
			@SuppressWarnings("unchecked")
			List<T> entityList = (List<T>)findMeth.invoke(repo, varValue);
			int i = entityList.size();
			if (i > 0)
			{
				Class<?> entityCls = entityList.get(0).getClass();
				Method statusMeth = entityCls.getMethod("getStatus", new Class<?>[0]);
				if (!statusMeth.getReturnType().equals(checkStatus.getClass()))
				{
					this.errMsg = this.funcName + ": "+entityCls.getSimpleName()+".getStatus does not returning correct type: "+varValue;
					setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					this.logger.logMessage(this.errMsg, LogLevel.ERROR);
					this.errVarDispName = entityCls.getSimpleName();
					this.errVarValue = varValue;
					this.errFuncDesc = entityCls.getSimpleName()+".getStatus does not returning correct type";
					return true;
				}
				while (i-- > 0)
				{
					T entity = entityList.get(i);
					Enum<?> status = (Enum<?>)statusMeth.invoke(entity);
					if (invalidCheck)
					{
						if (status != checkStatus)
						{
							this.errMsg = this.funcName + ": "+entityCls.getSimpleName()+" already exist: "+varValue;
							setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
							this.logger.logMessage(this.errMsg, LogLevel.ERROR);
							this.errVarDispName = entityCls.getSimpleName();
							this.errVarValue = varValue;
							this.errFuncDesc = entityCls.getSimpleName()+" already exist: "+varValue;
							return true;
						}
					}
					else
					{
						if (status == checkStatus)
						{
							this.errMsg = this.funcName + ": "+entityCls.getSimpleName()+" already exist: "+varValue;
							setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
							this.logger.logMessage(this.errMsg, LogLevel.ERROR);
							this.errVarDispName = entityCls.getSimpleName();
							this.errVarValue = varValue;
							this.errFuncDesc = entityCls.getSimpleName()+" already exist: "+varValue;
							return true;
						}
					}
				}
			}
			return false;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			this.errMsg = this.funcName + ": Method "+repo.getClass().getSimpleName()+"."+findFunc+"(String) not found";
			setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = varName;
			this.errVarValue = varValue;
			this.errFuncDesc = "Method "+repo.getClass().getSimpleName()+"."+findFunc+"(String) not found";
			return true;
		}
	}

	public <T> T getEntity(Class<T> cls, JpaRepository<T, Integer> repo, int id, Enum<?> checkingStatus, boolean invalidCheck)
	{
		if (this.errMsg != null) return null;
		Optional<T> entityObj = repo.findById(id);
		if (entityObj.isEmpty())
		{
			this.errMsg = this.funcName + ": "+cls.getSimpleName()+" not found: "+id;
			setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
			this.logger.logMessage(this.errMsg, LogLevel.ERROR);
			this.errVarDispName = cls.getSimpleName();
			this.errVarValue = ""+id;
			this.errFuncDesc = cls.getSimpleName()+" "+id+" not found";
			return null;
		}
		if (checkingStatus != null)
		{
			try
			{
				Method meth = cls.getMethod("getStatus", new Class<?>[0]);
				if (!meth.getReturnType().equals(checkingStatus.getClass()))
				{
					this.errMsg = this.funcName + ": "+cls.getSimpleName()+".getStatus does not returning correct type: "+id;
					setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					this.logger.logMessage(this.errMsg, LogLevel.ERROR);
					this.errVarDispName = cls.getSimpleName();
					this.errVarValue = ""+id;
					this.errFuncDesc = cls.getSimpleName()+".getStatus does not returning correct type";
					return null;
				}
				if (invalidCheck)
				{
					if (meth.invoke(entityObj.get()) == checkingStatus)
					{
						this.errMsg = this.funcName + ": "+cls.getSimpleName()+" not found: "+id;
						setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
						this.logger.logMessage(this.errMsg, LogLevel.ERROR);
						this.errVarDispName = cls.getSimpleName();
						this.errVarValue = ""+id;
						this.errFuncDesc = cls.getSimpleName()+" "+id+" is "+checkingStatus.toString();
						return null;
					}
				}
				else
				{
					if (meth.invoke(entityObj.get()) != checkingStatus)
					{
						this.errMsg = this.funcName + ": "+cls.getSimpleName()+" not found: "+id;
						setRespStatus(HttpServletResponse.SC_BAD_REQUEST);
						this.logger.logMessage(this.errMsg, LogLevel.ERROR);
						this.errVarDispName = cls.getSimpleName();
						this.errVarValue = ""+id;
						this.errFuncDesc = cls.getSimpleName()+" "+id+" is not "+checkingStatus.toString();
						return null;
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				this.errMsg = this.funcName + ": "+cls.getSimpleName()+" does not have getStatus: "+id;
				setRespStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				this.logger.logMessage(this.errMsg, LogLevel.ERROR);
				this.errVarDispName = cls.getSimpleName();
				this.errVarValue = ""+id;
				this.errFuncDesc = cls.getSimpleName()+" does not have getStatus";
				return null;
			}
		}
		return entityObj.get();
	}
}
