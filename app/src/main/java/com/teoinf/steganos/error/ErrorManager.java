package com.teoinf.steganos.error;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.teoinf.steganos.R;

public class ErrorManager {

	private static ErrorManager s_instance;
	
	public static ErrorManager getInstance() {
		if (s_instance == null) {
			s_instance = new ErrorManager();
		}
		return s_instance;
	}

	
	private List<String> _errorMessages;
	private List<Integer> _codeErrorMessages;
	
	private ErrorManager() {
		_errorMessages = new ArrayList<String>();
		_codeErrorMessages = new ArrayList<Integer>();
	}
	
	public void addErrorMessage(String message) {
		if (_errorMessages != null) {
			_errorMessages.add(message);
		}
	}
	
	public void addErrorMessage(int code) {
		if (_codeErrorMessages != null) {
			_codeErrorMessages.add(code);
		}
	}

	private void getErrorMessagesFromCode(Context context) {
		if (_codeErrorMessages == null || context == null 
				|| _errorMessages == null || _codeErrorMessages.isEmpty()) {
			return;
		}
		for (Integer i : _codeErrorMessages) {
			_errorMessages.add(context.getResources().getString(i));
		}
	}
	
	public void displayErrorMessages(Context context) {
		AlertDialog dialog;
		AlertDialog.Builder builder;
		LayoutInflater inflater;
		View view;
		ListView listView;
		
		getErrorMessagesFromCode(context);
		if (context == null || _errorMessages == null || _errorMessages.isEmpty()) {
			return;
		}

		builder = new AlertDialog.Builder(context);
		builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.dialog_error, null);
				
		listView = (ListView) view.findViewById(R.id.list_view_items);
		listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new ArrayList<String>(_errorMessages)));
		listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		_errorMessages.clear();
		_codeErrorMessages.clear();
				
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
	}
	
}
