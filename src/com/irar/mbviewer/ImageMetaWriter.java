package com.irar.mbviewer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class ImageMetaWriter {

	public byte[] writeCustomData(BufferedImage bi, HashMap<String, String> kv) throws Exception {
	    ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

	    ImageWriteParam writeParam = writer.getDefaultWriteParam();
	    ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

	    //adding metadata
	    IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	    IIOMetadataNode text = new IIOMetadataNode("tEXt");
	    int i = 0;
	    for(String key : kv.keySet()) {
	    	String value = kv.get(key);
		    IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry" + i);
		    textEntry.setAttribute("keyword", key);
		    textEntry.setAttribute("value", value);
		    text.appendChild(textEntry);
	    	i++;
	    }
	    
	    IIOMetadataNode root = new IIOMetadataNode("iaz_png_data_1.0");
	    root.appendChild(text);

	    metadata.mergeTree("iaz_png_data_1.0", root);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
	    writer.setOutput(stream);
	    writer.write(metadata, new IIOImage(bi, null, metadata), writeParam);
	    stream.close();

	    return baos.toByteArray();
	}
	
}
