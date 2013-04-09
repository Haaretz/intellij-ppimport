package be.mavicon.intellij.ppimport;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.RunBackgroundable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/*
 * Copyright 2013 Marc Viaene (Mavicon BVBA)
 * Copyright 2013 Wim Symons (wim.symons@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
@SuppressWarnings("ComponentNotRegistered")
class PPImportAction extends AnAction {

	private Target target = null;
	private List<String> includeExtensions;
	private boolean uploadMultipleFilesAsJar;

	public PPImportAction(Target target, List<String> includeExtensions, boolean uploadMultipleFilesAsJar) {
		super(target.getProfile(), "Import content to " + target.getUrl(), null);
		this.target = target;
		this.includeExtensions = includeExtensions;
		this.uploadMultipleFilesAsJar = uploadMultipleFilesAsJar;
	}

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		final DataContext dataContext = event.getDataContext();
		VirtualFile[] files = getSelectedFiles(dataContext);
		event.getPresentation().setEnabled(isValidSelection(files));
	}

	public void actionPerformed(AnActionEvent e) {
		VirtualFile[] virtualFiles = e.getData(LangDataKeys.VIRTUAL_FILE_ARRAY);
		if (isValidSelection(virtualFiles)) {
			if (target.isConfirm()) {
				int answer = Messages.showYesNoDialog("Are you sure to import the selected files into " + target.getProfile() + "?", "Confirm Polopoly Import", Messages.getWarningIcon());
				if (answer == Messages.NO) {
					PPImportPlugin.doNotify("Import cancelled upon confirmation.", NotificationType.INFORMATION);
					return;
				}
			}
			RunBackgroundable.run(new ImportTask(virtualFiles, this.target, this.includeExtensions, this.uploadMultipleFilesAsJar));
		}
	}

	private VirtualFile[] getSelectedFiles(final DataContext dataCtx) {
		final VirtualFile[] result = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataCtx);
		return result != null ? result : new VirtualFile[0];
	}

	private boolean isValidSelection(VirtualFile[] selection) {
		for (VirtualFile virtualFile : selection) {
			if (virtualFile.isDirectory() || virtualFile.isInLocalFileSystem() && includeExtensions.contains(virtualFile.getExtension())) {
				return true;
			}
		}
		return false;
	}
}
