/* (C) 2009 Mathijs Vogelzang/University Medical Center Groningen.
 * For more information go to http://qube.sf.net/ 
 * 
 * This file is part of QuBE (Quantitative Blush Evaluator).
 * 
 * QuBE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * QuBE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with QuBE.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.umcg.qube.io;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;

/**
 * @author mathijs
 * 
 */
public class DicomUtil {
    public static BufferedImage extractThumbFrame(File file) {
        BufferedImage res = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        DicomInputStream in = null;
        try {
            try {
                in = new DicomInputStream(new BufferedInputStream(new FileInputStream(file)));
                AttributeList al = new AttributeList();
                al.read(in, TagFromName.PixelData);

                // cur = new Date().getTime();
                // System.out.println("first tags read "+ (cur-start));
                // start = cur;

                int frames = ((Attribute) al.get(TagFromName.NumberOfFrames))
                        .getSingleIntegerValueOrDefault(2);
                int indexFrame = frames / 2;
                // in.mark(1000000);
                // in.read

                in.readSigned32();
                in.readSigned32();

                String readerWanted = "jpeg-lossless";
                // readerWanted="jpeg-ls";
                //

                ImageReader reader = (ImageReader) (ImageIO
                        .getImageReadersByFormatName(readerWanted).next());

                // cur = new Date().getTime();
                // System.out.println("Reader insted "+ (cur-start));
                // start = cur;
                in.mark(4000);
                in.readUnsigned16();
                in.readUnsigned16();
                long len = in.readUnsigned32();
                // byte[] bts = new byte[(int) len];
                long off = 0;
                for (int i = 0; i < indexFrame; i++)
                    off = in.readUnsigned32();
                in.reset();

                in.skipInsistently(8 + len + 8 + off);

                BufferedImage image = null;
                ImageInputStream iiois = ImageIO.createImageInputStream(in);
                reader.setInput(iiois, true/* seekForwardOnly */, true/* ignoreMetadata */);

                image = reader.read(0);
                res.getGraphics().drawImage(image, 0, 0, 256, 256, null);

                res.getGraphics().drawString(file.getName(), 0, 10);
                res.getGraphics().drawString(
                        al.get(TagFromName.PatientName).getSingleStringValueOrDefault(""), 0, 20);
                res.getGraphics().drawString(
                        al.get(TagFromName.ContentTime) == null ? "" : al.get(
                                TagFromName.ContentTime).getSingleStringValueOrDefault(""), 0, 30);
            } finally {
                if (in != null)
                    in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Graphics g = res.getGraphics();
            g.setColor(Color.BLACK);
            g.drawString("Could not open file.", 20, 20);
        }
        return res;
    }
}
