//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                    G l y p h C o n t e n t                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph.facets;

import omr.text.TextRole;
import omr.text.TextWord;

/**
 * Interface {@code GlyphContent} defines a facet that deals with the textual content,
 * if any, of a glyph.
 *
 * @author Hervé Bitteur
 */
public interface GlyphContent
        extends GlyphFacet
{
    //~ Instance fields ----------------------------------------------------------------------------

    /** String equivalent of Character used for elision. (undertie) */
    String ELISION_STRING = new String(Character.toChars(8255));

    /** String equivalent of Character used for extension. (underscore) */
    String EXTENSION_STRING = "_";

    /** String equivalent of Character used for hyphen. */
    String HYPHEN_STRING = "-";

    //~ Methods ------------------------------------------------------------------------------------
    /**
     * Report the string value of this text glyph if any.
     *
     * @return the text meaning of this glyph if any, which is the manual value
     *         if any, or the ocr value otherwise.
     */
    String getTextValue ();

    /**
     * Return the corresponding text word for this glyph, if any.
     *
     * @return the related text word, null otherwise.
     */
    TextWord getTextWord ();

    /**
     * Manually assign a text role to the glyph.
     *
     * @param manualRole the role for this text glyph
     */
    void setManualRole (TextRole manualRole);

    /**
     * Manually assign a text meaning to the glyph.
     *
     * @param manualValue the string value for this text glyph
     */
    void setManualValue (String manualValue);

    /**
     * Set the related text word.
     *
     * @param ocrLanguage the language provided to OCR engine for recognition
     * @param textWord    the TextWord for this glyph
     */
    void setTextWord (String ocrLanguage,
                      TextWord textWord);
}
