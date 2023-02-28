package com.xy.management;

import java.util.HashMap;
import java.util.Map;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.workflow.WorkflowService;
import com.teamcenter.services.strong.workflow._2007_06.Workflow;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.EPMConditionTask;
import com.teamcenter.soa.client.model.strong.EPMDoTask;
import com.teamcenter.soa.client.model.strong.EPMPerformSignoffTask;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

public class WorkflowManagement {

    private Connection m_connection;
    private DataManagementService dmService;
    private WorkflowService workflowService;

    public WorkflowManagement(Connection connection) {
        m_connection = connection;
        dmService = DataManagementService.getService(m_connection);
        workflowService = WorkflowService.getService(m_connection);
    }

    /**
     * @param modelObjects
     * @param releaseName（Released/TCM Released�?
     * @throws Exception
     */
    public void releaseObject(ModelObject[] modelObjects, String releaseName) throws Exception {
        releaseOperation(modelObjects, releaseName, true);
    }

    /**
     * @param modelObjects
     * @param releaseName（Released/TCM Released�?
     * @throws Exception
     */
    public void unReleaseObject(ModelObject[] modelObjects, String releaseName) throws Exception {
        releaseOperation(modelObjects, releaseName, false);
    }

    private void releaseOperation(ModelObject[] modelObjects, String releaseName, boolean release) throws Exception {
        Workflow.ReleaseStatusOption[] option = new Workflow.ReleaseStatusOption[1];
        option[0] = new Workflow.ReleaseStatusOption();
        option[0].existingreleaseStatusTypeName = releaseName;
        option[0].newReleaseStatusTypeName = releaseName;
        if (release) {
            option[0].operation = "Append";
        } else {
            option[0].operation = "Delete";
        }
        Workflow.ReleaseStatusInput releaseStatusInput = new Workflow.ReleaseStatusInput();
        WorkspaceObject[] workspaceObjects = new WorkspaceObject[modelObjects.length];
        for (int i = 0; i < modelObjects.length; i++) {
            workspaceObjects[i] = (WorkspaceObject) modelObjects[i];
        }
        releaseStatusInput.objects = workspaceObjects;
        releaseStatusInput.operations = option;
        Workflow.SetReleaseStatusResponse response = workflowService.setReleaseStatus(new Workflow.ReleaseStatusInput[]{releaseStatusInput});
        if (response.serviceData.sizeOfPartialErrors() > 0) {
            throw new Exception("WorkflowManagement.releaseOperation error: " +  response.serviceData.toString());
        }
    }

    public void performEPMPerformSignoffTaskWithApprove(EPMPerformSignoffTask task, String comment, String password, User user) throws Exception {
        performEPMPerformSignoffTask(task, comment, password, "SOA_EPM_approve_action", "SOA_EPM_approve", user);
    }

    public void performEPMPerformSignoffTaskWithReject(EPMPerformSignoffTask task, String comment, String password, User user) throws Exception {
        performEPMPerformSignoffTask(task, comment, password, "SOA_EPM_reject_action", "SOA_EPM_reject", user);
    }

    public void performEPMPerformSignoffTaskWithDemote(EPMPerformSignoffTask task, String comment, String password, User user) throws Exception {
        performEPMPerformSignoffTask(task, comment, password, "SOA_EPM_demote_action", "SOA_EPM_demote", user);
    }

    private void performEPMPerformSignoffTask(EPMPerformSignoffTask task, String comment, String password, String action, String supportingValue, User user) throws Exception {
        dmService.getProperties(new ModelObject[] { task }, new String[] { "signoff_attachments" });
        ModelObject[] signoff_attachments = task.get_signoff_attachments();
        dmService.getProperties(signoff_attachments, new String[] { "group_member" });
        boolean performSuccess = false;
        for (ModelObject signoff_attachment : task.get_signoff_attachments()) {
            ModelObject group_member = signoff_attachment.getPropertyObject("group_member").getModelObjectValue();
            dmService.getProperties(new ModelObject[] { group_member }, new String[] { "the_user" });
//            ModelObject the_user = group_member.getPropertyObject("the_user").getModelObjectValue();
//            if (true||the_user.equals(user)) {
                performTask(signoff_attachment, action, supportingValue, comment, password);
                performSuccess = true;
//            }
        }
        if (!performSuccess) {
            throw new Exception("当前登录的用户无法执行该任务");
        }
    }

    public void performEPMDoTask(EPMDoTask task, String comment, String password) throws Exception {
        performTask(task, "SOA_EPM_complete_action", "SOA_EPM_completed", comment, password);
    }

    public void performEPMConditionTask(EPMConditionTask task, String conditionBranch, String comment, String password) throws Exception {
        performTask(task, "SOA_EPM_complete_action", conditionBranch, comment, password);
    }

    private void performTask(ModelObject task, String action, String supportingValue, String comment, String password) throws Exception {
        com.teamcenter.services.strong.workflow._2014_06.Workflow.PerformActionInputInfo performActionInputInfo = new com.teamcenter.services.strong.workflow._2014_06.Workflow.PerformActionInputInfo();
        performActionInputInfo.clientId = "clientTask";
        performActionInputInfo.action = action;
        performActionInputInfo.actionableObject = task;
        performActionInputInfo.password = password;
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("comments", new String[] { comment });
        performActionInputInfo.propertyNameValues = properties;
        performActionInputInfo.supportingValue = supportingValue;

        WorkflowService workflowService = WorkflowService.getService(m_connection);
        ServiceData serviceData = workflowService.performAction3(new com.teamcenter.services.strong.workflow._2014_06.Workflow.PerformActionInputInfo[] { performActionInputInfo });
        if (serviceData.sizeOfPartialErrors() > 0) {
            throw new Exception("WorkflowService.performTask.performAction3 error: " + serviceData.toString());
        }
    }
}
