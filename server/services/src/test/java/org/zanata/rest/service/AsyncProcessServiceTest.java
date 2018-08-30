/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.async.UserTriggeredTaskHandle;
import org.zanata.exception.AuthorizationException;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.security.ZanataIdentity;

import com.google.common.collect.Maps;

public class AsyncProcessServiceTest {

    private AsyncProcessService service;
    @Mock
    private UriInfo urlInfo;
    @Mock
    private AsyncTaskHandleManager taskHandleManager;
    @Mock
    private ZanataIdentity identity;
    private String baseUriStr;
    private String currentUsername;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        service = new AsyncProcessService(taskHandleManager, identity, urlInfo);
        this.baseUriStr = "http://localhost/rest/";
        currentUsername = "test-user";
        when(identity.getAccountUsername()).thenReturn(currentUsername);
    }

    @Test
    public void returnNotFoundIfProcessTaskIdCanNotBeFound() {
        Response response = service.getAsyncProcessStatus("notFoundId");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void notAdminUserCanReturnHisProcessStatus() throws URISyntaxException {
        UserTriggeredStartedAsyncTaskHandle taskHandle = new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy(currentUsername);
        taskHandle.setMaxProgress(100L);
        taskHandle.increaseProgress(90);

        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.getAsyncProcessStatus("id");

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getPercentageComplete()).isEqualTo(90);

    }

    @Test
    public void notAdminUserCanNotSeeOthersProcessStatus() throws URISyntaxException {
        UserTriggeredStartedAsyncTaskHandle taskHandle = new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy("somebody-else");

        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.getAsyncProcessStatus("id");

        assertThat(response.getStatus()).isEqualTo(404);
    }



    @Test
    public void adminUserCanSeeOthersProcessStatus() throws URISyntaxException {
        UserTriggeredStartedAsyncTaskHandle taskHandle = new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy("somebody-else");

        when(identity.hasRole("admin")).thenReturn(true);
        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.getAsyncProcessStatus("id");

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode()).isEqualTo(
                ProcessStatus.ProcessStatusCode.Running);
    }

    @Test
    public void notAdminUserCanNotSeeSystemProcessStatus() throws URISyntaxException {
        AsyncTaskHandle<Object> taskHandle = new AsyncTaskHandle<>();

        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.getAsyncProcessStatus("id");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void adminUserCanSeeSystemProcessStatus() throws URISyntaxException {
        AsyncTaskHandle<Object> taskHandle = new AsyncTaskHandle<>();

        when(identity.hasRole("admin")).thenReturn(true);
        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.getAsyncProcessStatus("id");

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode()).isEqualTo(
                ProcessStatus.ProcessStatusCode.Running);
    }

    @Test
    public void canGetAllRunningTasks() throws URISyntaxException {
        when(urlInfo.getBaseUri()).thenReturn(new URI(baseUriStr));

        Map<String, AsyncTaskHandle<?>> runningTasks =
                Maps.newHashMap();

        String id = "keyId";
        AsyncTaskHandle<String> taskHandle = new AsyncTaskHandle<>();
        runningTasks.put(id, taskHandle);

        when(taskHandleManager.getRunningTasks()).thenReturn(runningTasks);
        Response response = service.getAllAsyncProcessStatuses(false);

        assertThat(response.getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<ProcessStatus> statusList =
                (List<ProcessStatus>) response.getEntity();
        assertThat(statusList).hasSize(1);
        assertThat(statusList.get(0).getUrl())
                .isEqualTo(baseUriStr + "process/key?keyId=keyId");
    }

    /**
     * This operation is only available to admin.
     * @see org.zanata.rest.service.raw.AsyncProcessRawRestITCase
     */
    @Test
    public void canGetAllTasksIncludingFinishedTask()
            throws URISyntaxException {
        when(urlInfo.getBaseUri()).thenReturn(new URI(baseUriStr));

        Map<String, AsyncTaskHandle<?>> runningTasks =
                Maps.newHashMap();

        String id = "keyId";
        AsyncTaskHandle<String> taskHandle = new AsyncTaskHandle<>();
        runningTasks.put(id, taskHandle);

        when(taskHandleManager.getAllTasks()).thenReturn(runningTasks);
        Response response = service.getAllAsyncProcessStatuses(true);

        assertThat(response.getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<ProcessStatus> statusList =
                (List<ProcessStatus>) response.getEntity();
        assertThat(statusList).hasSize(1);
        assertThat(statusList.get(0).getUrl())
                .isEqualTo(baseUriStr + "process/key?keyId=keyId");
    }

    @Test
    public void returnNotFoundIfCancelTaskCanNotBeFound() {
        Response response = service.cancelAsyncProcess("notFoundId");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void returnTaskStatusIfTaskIsNotRunning() throws URISyntaxException {
        // we have a finished task
        AsyncTaskHandle<Object> taskHandle =
                new FinishedAsyncTaskHandle("result");

        when(taskHandleManager.getHandleByKeyId("id")).thenReturn(taskHandle);
        when(urlInfo.getRequestUri())
                .thenReturn(new URI(baseUriStr + "process/id"));
        Response response = service.cancelAsyncProcess("id");

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode())
                .isEqualTo(ProcessStatus.ProcessStatusCode.Finished);
    }

    @Test
    public void notAdminUserCanNotCancelSystemTask() {
        AsyncTaskHandle<Object> taskHandle = new StartedAsyncTaskHandle();

        String keyId = "id";
        when(taskHandleManager.getHandleByKeyId(keyId)).thenReturn(taskHandle);

        assertThatThrownBy(() -> service.cancelAsyncProcess(keyId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Only admin can cancel the task:" + keyId);
    }

    @Test
    public void adminUserCanCancelSystemTask() {
        AsyncTaskHandle<Object> taskHandle = new StartedAsyncTaskHandle();

        String keyId = "id";
        when(taskHandleManager.getHandleByKeyId(keyId)).thenReturn(taskHandle);
        when(identity.hasRole("admin")).thenReturn(true);

        Response response = service.cancelAsyncProcess(keyId);

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode())
                .isEqualTo(ProcessStatus.ProcessStatusCode.Cancelled);
        assertThat(processStatus.getMessages())
                .contains("Cancelled by " + currentUsername);
    }

    @Test
    public void notAdminUserCanNotCancelOthersTask() {
        UserTriggeredStartedAsyncTaskHandle taskHandle =
                new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy("someone-else");

        String keyId = "id";
        when(taskHandleManager.getHandleByKeyId(keyId)).thenReturn(taskHandle);

        assertThatThrownBy(() -> service.cancelAsyncProcess(keyId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Only the task owner or admin can cancel the task:"
                        + keyId);
    }

    @Test
    public void adminUserCanCancelOtherUsersTask() {
        UserTriggeredStartedAsyncTaskHandle taskHandle =
                new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy("someone-else");

        when(identity.hasRole("admin")).thenReturn(true);
        String keyId = "id";
        when(taskHandleManager.getHandleByKeyId(keyId)).thenReturn(taskHandle);

        Response response = service.cancelAsyncProcess(keyId);

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode())
                .isEqualTo(ProcessStatus.ProcessStatusCode.Cancelled);
        assertThat(processStatus.getMessages())
                .contains("Cancelled by " + currentUsername);
    }

    @Test
    public void sameUserCanCancelHisOwnTask() {
        UserTriggeredStartedAsyncTaskHandle taskHandle =
                new UserTriggeredStartedAsyncTaskHandle();
        taskHandle.setTriggeredBy(currentUsername);

        when(identity.hasRole("admin")).thenReturn(false);
        String keyId = "id";
        when(taskHandleManager.getHandleByKeyId(keyId)).thenReturn(taskHandle);

        Response response = service.cancelAsyncProcess(keyId);

        assertThat(response.getStatus()).isEqualTo(200);
        ProcessStatus processStatus = (ProcessStatus) response.getEntity();
        assertThat(processStatus.getStatusCode())
                .isEqualTo(ProcessStatus.ProcessStatusCode.Cancelled);
        assertThat(processStatus.getMessages())
                .contains("Cancelled by " + currentUsername);
    }

    private static class FinishedAsyncTaskHandle
            extends AsyncTaskHandle<Object> {
        private static final long serialVersionUID = 1L;

        private FinishedAsyncTaskHandle(String result) {
            CompletableFuture<Object> futureResult = new CompletableFuture<>();
            futureResult.complete(result);
            super.setFutureResult(futureResult);
        }
    }

    private static class StartedAsyncTaskHandle
            extends AsyncTaskHandle<Object> {
        private static final long serialVersionUID = 1L;
        private boolean cancelled;

        private StartedAsyncTaskHandle() {
            // move the process status to started
            startTiming();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    private static class UserTriggeredStartedAsyncTaskHandle extends
            StartedAsyncTaskHandle implements UserTriggeredTaskHandle {

        private static final long serialVersionUID = 1L;
        private String triggerBy;

        @Override
        public void setTriggeredBy(String username) {
            this.triggerBy = username;
        }

        @Override
        public String getTriggeredBy() {
            return triggerBy;
        }

        @Override
        public String getTaskName() {
            return "TestAsyncTaskName";
        }
    }
}
