package com.teoinf.steganos.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import android.content.Context;

import com.teoinf.steganos.algorithms.IDataAlgorithm;
import com.teoinf.steganos.algorithms.data.CryptographyAlgorithmData;
import com.teoinf.steganos.algorithms.data.SteganographyAlgorithmData;
import com.teoinf.steganos.algorithms.data.SteganographyAlgorithmData.SteganographyChannelType;

public class Configuration {

	private static Configuration s_instance = null;
	
	public static Configuration getInstance() {
		if (s_instance == null) {
			s_instance = new Configuration();
		}
		return s_instance;
	}

	private final String RAW_PATH = "raw";
	private final String AUDIO_XML_PATH = "audio";
	private final String VIDEO_XML_PATH = "video";
	private final String CRYPTO_XML_PATH = "crypto";
	private final String METADATA_XML_PATH = "metadata";
	private final String CHILD_STEGANOGRAPHY_ALGORITHM = "algorithm";
	private final String CHILD_CRYPTOGRAPHY_ALGORITHM = "algorithm";
	
	private List<SteganographyAlgorithmData> _steganographyAlgorithms;
	private List<CryptographyAlgorithmData> _cryptographyAlgorithms;
	
	private Configuration() {
		_steganographyAlgorithms = new ArrayList<SteganographyAlgorithmData>();
		_cryptographyAlgorithms = new ArrayList<CryptographyAlgorithmData>();
	}
	
	public boolean loadData(Context context){
		boolean ret = true;
		
		if (context == null) {
			return false;
		}
		ret &= loadSteganographyAlgorithmData(context, AUDIO_XML_PATH, SteganographyChannelType.AUDIO);
		ret &= loadSteganographyAlgorithmData(context, VIDEO_XML_PATH, SteganographyChannelType.VIDEO);
		ret &= loadSteganographyAlgorithmData(context, METADATA_XML_PATH, SteganographyChannelType.METADATA);
		ret &= loadCryptographyAlgorithmData(context, CRYPTO_XML_PATH);
		return ret;
	}
	
	private boolean loadSteganographyAlgorithmData(Context context, String path, SteganographyChannelType type) {
		SAXBuilder builder = new SAXBuilder();
		int identifier;
		InputStream input;
		boolean ret = false;
		
		identifier = context.getResources().getIdentifier(RAW_PATH + '/' + path, RAW_PATH, context.getPackageName());
		input = context.getResources().openRawResource(identifier);
	
		try {
			Document document = (Document) builder.build(input);
			Element rootNode = document.getRootElement();
			List<Element> list = rootNode.getChildren(CHILD_STEGANOGRAPHY_ALGORITHM);
	 
			for (int i = 0; i < list.size(); i++) {
			   Element node = (Element) list.get(i);
			   SteganographyAlgorithmData data = new SteganographyAlgorithmData();
			   data.setDisplayName(node.getChildText("display"));
			   data.setPath(node.getChildText("path"));
			   data.setSteganographyChannelType(type);
			   if (_steganographyAlgorithms != null) {
				   _steganographyAlgorithms.add(data);
			   }
			}
		   input.close();
		   ret = true;
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
		return ret;
	}
	
	private boolean loadCryptographyAlgorithmData(Context context, String path) {
		SAXBuilder builder = new SAXBuilder();
		int identifier;
		InputStream input;
		boolean ret = false;
		
		identifier = context.getResources().getIdentifier(RAW_PATH + '/' + path, RAW_PATH, context.getPackageName());
		input = context.getResources().openRawResource(identifier);
	
		try {
			Document document = (Document) builder.build(input);
			Element rootNode = document.getRootElement();
			List<Element> list = rootNode.getChildren(CHILD_CRYPTOGRAPHY_ALGORITHM);
	 
			for (int i = 0; i < list.size(); i++) {
			   Element node = (Element) list.get(i);
			   CryptographyAlgorithmData data = new CryptographyAlgorithmData();
			   data.setDisplayName(node.getChildText("display"));
			   data.setKeyLength(Integer.parseInt(node.getChildText("keylength")));
			   data.setPath(node.getChildText("path"));
			   if (_cryptographyAlgorithms != null) {
				   _cryptographyAlgorithms.add(data);
			   }
			}
		   input.close();
		   ret = true;
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
		return ret;
	}
		
	public List<IDataAlgorithm> getSteganographyAlgorithmByType(SteganographyChannelType type) {
		List<IDataAlgorithm> ret = new ArrayList<IDataAlgorithm>();
		
		if (_steganographyAlgorithms != null) {
			for (SteganographyAlgorithmData tmp : _steganographyAlgorithms) {
				if (tmp.getSteganographyChannelType() == type) {
					ret.add(tmp);
				}
			}
		}
		return ret;
	}
	
	public List<IDataAlgorithm> getCryptographyAlgorithms() {
		if (_cryptographyAlgorithms == null) {
			return new ArrayList<IDataAlgorithm>();
		}
		return new ArrayList<IDataAlgorithm>(_cryptographyAlgorithms);
	}
	
	public CryptographyAlgorithmData getCryptographyAlgorithmDataByPath(String path) {
		if (_cryptographyAlgorithms == null || path == null) {
			return null;
		}
		for (CryptographyAlgorithmData tmp : _cryptographyAlgorithms) {
			if (tmp.getPath().equals(path)) {
				return tmp;
			}
		}
		return null;
	}
}
