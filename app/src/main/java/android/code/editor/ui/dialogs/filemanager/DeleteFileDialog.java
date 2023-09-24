/*
 *  This file is part of Android Code Editor.
 *
 *  Android Code Editor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android Code Editor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Android Code Editor.  If not, see <https://www.gnu.org/licenses/>.
 */

package android.code.editor.ui.dialogs.filemanager;

import android.code.editor.R;
import android.code.editor.common.interfaces.FileDeleteListener;
import android.code.editor.common.utils.FileDeleteUtils;
import android.code.editor.databinding.LayoutDeletingFileBinding;
import android.code.editor.ui.activities.FileManagerActivity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import android.net.Uri;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteFileDialog {
  public FileManagerActivity activity;
  public int position;
  public File path;
  public AlertDialog alert;
  public LayoutDeletingFileBinding binding;
  public FDL fdl;

  public DeleteFileDialog(FileManagerActivity activity, File path, int position) {
    this.activity = activity;
    this.position = position;
    this.path = path;
    fdl = new FDL(activity);
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
    dialog.setTitle(R.string.delete);
    dialog.setMessage(R.string.do_you_want_to_delete_the_selected_path);
    dialog.setPositiveButton(
        R.string.delete,
        (param1, param2) -> {
          delete();
        });
    dialog.setNegativeButton(R.string.cancel, (param1, param2) -> {});
    dialog.create().show();
  }

  public void delete(final int position) {
    binding = LayoutDeletingFileBinding.inflate(activity.getLayoutInflater());
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
    dialog.setTitle("Delete " + Uri.fromFile(new File(adapter.getItem(position))).getLastPathSegment() + "?");
    dialog.setMessage(R.string.please_wait);
    dialog.setView(binding.getRoot());
    alert = dialog.create();
    dialog.setCancelable(false);
    alert.show();
    if (path.isDirectory()) {
      FileDeleteUtils.getTotalCount(path, fdl, FileDeleteUtils.count, true, activity);
    } else {
      ExecutorService exec = Executors.newSingleThreadExecutor();
      exec.execute(
          new Runnable() {
            @Override
            public void run() {
              activity.runOnUiThread(
                  () -> {
                    binding.progressbar.setIndeterminate(true);
                  });
              path.delete();
              activity.runOnUiThread(
                  () -> {
                    alert.dismiss();
                    activity.listMap.clear();
                    activity.listString.clear();
                    activity.loadFileList(activity.currentDir);
                  });
            }
          });
    }
  }

  public class FDL implements FileDeleteListener {
    private Context context;

    public FDL(Context c) {
      context = c;
    }

    @Override
    public void onProgressUpdate(int deleteDone) {}

    @Override
    public void onTotalCount(int total) {
      FileDeleteUtils.delete(path, fdl, true, activity);
      binding.progressbar.setMax(total);
    }

    @Override
    public void onDeleting(File path) {
      binding.tvPath.setText(path.getAbsolutePath());
    }

    @Override
    public void onDeleteComplete(File path) {
      binding.progressbar.setProgress(binding.progressbar.getProgress() + 1);
    }

    @Override
    public void onTaskComplete() {
      alert.dismiss();
      activity.listMap.clear();
      activity.listString.clear();
      activity.loadFileList(activity.currentDir);
    }
  }
}
