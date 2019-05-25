package com.teoinf.steganos.directorydialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DirectoryDialog {

	private Context _context;
	private String _currentDirectory;
	private ArrayAdapter<String> _adapter;
	private ChoosenDirectoryListener _listener;
	
	private LinearLayout _layout;
	private TextView _textViewTitle;
	private TextView _textViewCurrentPath;
	
	public DirectoryDialog(Context context) {
		_context = context;
		_currentDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	public DirectoryDialog(Context context, ChoosenDirectoryListener listener) {
		_context = context;
		_currentDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
		_listener = listener;
	}
	
	public void show() {
	    AlertDialog.Builder builder;
	    
	    if (_context == null) {
	    	Log.d("DEBUG", "Directory Dialog: Unable to create dialog: Context is null");
	    	return;
	    }
	    builder = new AlertDialog.Builder(_context);
	    this.initBuilderLayout(builder);
	    builder.setPositiveButton("OK", onOkListener);
	    builder.setNegativeButton("Cancel", null);
	    
	    _adapter = new ArrayAdapter<String>(_context, android.R.layout.select_dialog_item, android.R.id.text1, this.getDirectories(_currentDirectory));
	    builder.setSingleChoiceItems(_adapter, -1, onClickListener);
	    
	    builder.show();
	
	}

	public void setChoosenDirectoryListener(ChoosenDirectoryListener listener) {
		_listener = listener;
	}
	
	private void initBuilderLayout(AlertDialog.Builder builder) {
		
		_textViewTitle = new TextView(_context);
		_textViewTitle.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		_textViewTitle.setTextAppearance(_context, android.R.style.TextAppearance_Large);
		_textViewTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		_textViewTitle.setText("Select directory");

		_textViewCurrentPath = new TextView(_context);
		_textViewCurrentPath.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		_textViewCurrentPath.setTextAppearance(_context, android.R.style.TextAppearance_Medium);
		_textViewCurrentPath.setText(_currentDirectory);
		
		_layout = new LinearLayout(_context);
		_layout.setOrientation(LinearLayout.VERTICAL);
		_layout.addView(_textViewTitle);
		_layout.addView(_textViewCurrentPath);
		
		builder.setCustomTitle(_layout);
	}
	
	private String constructPath(String currentPath, String directory) {
		StringBuilder sb;
		
		sb = new StringBuilder(currentPath);
		if (currentPath == null || directory == null || directory.isEmpty())
			return (sb.toString());
		
		if (currentPath.equals("/") && (directory.equals("..") || directory.equals("../")))
			return (sb.toString());
		
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		if ((directory.equals("..") || directory.equals("../"))) {
			sb.setCharAt(sb.length() - 1, '\0');
			sb = new StringBuilder(sb.substring(0, sb.lastIndexOf("/")));
		} else {
			sb.append(directory);
		}

		if (sb.toString().isEmpty() || sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}

		return (sb.toString());
	}
	
	private List<String> getDirectories(String path) {
		File current;
		List<String> directories;
		File[] files;
		
		current = new File(path);
		directories = new ArrayList<String>();
		directories.add("..");
		if (current.exists() && current.isDirectory()) {
			files = current.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						directories.add(f.getName());
					}
				}
			}
		}
		
		Collections.sort(directories, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return s1.compareToIgnoreCase(s2);
	        }
	    });
		
		return (directories);
	}
	
	private OnClickListener onOkListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			if (_listener != null) {
				_listener.onChoosenDir(_currentDirectory);
			}
		}
	};

	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface arg0, int selectedIndex) {
			if (selectedIndex == 0) {
				_currentDirectory = constructPath(_currentDirectory, "../");
			} else {
				_currentDirectory = constructPath(_currentDirectory, _adapter.getItem(selectedIndex));
			}
			_textViewCurrentPath.setText(_currentDirectory);
			_adapter.clear();
			_adapter.addAll(getDirectories(_currentDirectory));
		}
	};

	
}
