//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                     G l y p h s M o d e l                                      //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//  Copyright © Hervé Bitteur and others 2000-2014. All rights reserved.
//  This software is released under the GNU General Public License.
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph;

import omr.OMR;

import omr.glyph.facets.Glyph;

import omr.score.ui.ScoreActions;

import omr.sheet.Sheet;
import omr.sheet.SystemInfo;

import omr.step.Step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class {@code GlyphsModel} is a common model for synchronous glyph and section handling.
 * <p>
 * Nota: User gesture should trigger actions in GlyphsController which will asynchronously delegate
 * to this model.
 *
 * @author Hervé Bitteur
 */
public class GlyphsModel
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(GlyphsModel.class);

    //~ Instance fields ----------------------------------------------------------------------------
    /** Underlying glyph nest */
    protected final GlyphNest nest;

    /** Related Sheet */
    protected final Sheet sheet;

    /** Related Step */
    protected final Step step;

    /** Latest shape assigned if any */
    protected Shape latestShape;

    //~ Constructors -------------------------------------------------------------------------------
    /**
     * Create an instance of GlyphsModel, with its underlying glyph lag.
     *
     * @param sheet the related sheet (can be null)
     * @param nest  the related nest (cannot be null)
     * @param step  the step after which update should be perform (can be null)
     */
    public GlyphsModel (Sheet sheet,
                        GlyphNest nest,
                        Step step)
    {
        // Null sheet is allowed (for GlyphVerifier use)
        this.sheet = sheet;

        if (nest == null) {
            throw new IllegalArgumentException(
                    "Attempt to create a GlyphsModel with null underlying nest");
        } else {
            this.nest = nest;
        }

        this.step = step;
    }

    //~ Methods ------------------------------------------------------------------------------------
    //--------------//
    // assignGlyphs //
    //--------------//
    /**
     * Assign a shape to the selected collection of glyphs.
     *
     * @param glyphs   the collection of glyphs to be assigned
     * @param shape    the shape to be assigned
     * @param compound flag to build one compound, rather than assign each
     *                 individual glyph
     * @param grade    the grade we have wrt the assigned shape
     */
    public void assignGlyphs (Collection<Glyph> glyphs,
                              Shape shape,
                              boolean compound,
                              double grade)
    {
        if (compound) {
            // Build & insert one compound
            Glyph glyph = nest.buildGlyph(glyphs, true);

            assignGlyph(glyph, shape, grade);
        } else {
            // Assign each glyph individually
            for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
                if (glyph.getShape() != Shape.NOISE) {
                    assignGlyph(glyph, shape, grade);
                }
            }
        }
    }

    //----------------//
    // deassignGlyphs //
    //----------------//
    /**
     * De-Assign a collection of glyphs.
     *
     * @param glyphs the collection of glyphs to be de-assigned
     */
    public void deassignGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deassignGlyph(glyph);
        }
    }

    //--------------//
    // deleteGlyphs //
    //--------------//
    public void deleteGlyphs (Collection<Glyph> glyphs)
    {
        for (Glyph glyph : new ArrayList<Glyph>(glyphs)) {
            deleteGlyph(glyph);
        }
    }

    //--------------//
    // getGlyphById //
    //--------------//
    /**
     * Retrieve a glyph, knowing its id.
     *
     * @param id the glyph id
     * @return the glyph found, or null if not
     */
    public Glyph getGlyphById (int id)
    {
        return nest.getGlyph(id);
    }

    //----------------//
    // getLatestShape //
    //----------------//
    /**
     * Report the latest non null shape that was assigned, or null if
     * none.
     *
     * @return latest shape assigned, or null if none
     */
    public Shape getLatestShape ()
    {
        return latestShape;
    }

    //---------//
    // getGlyphNest //
    //---------//
    /**
     * Report the underlying glyph nest.
     *
     * @return the related glyph nest
     */
    public GlyphNest getNest ()
    {
        return nest;
    }

    //----------------//
    // getRelatedStep //
    //----------------//
    /**
     * Report the step this GlyphsModel is used for, so that we know
     * from which step updates must be propagated.
     * (we have to update the steps that follow this one)
     *
     * @return the step related to this glyphs model
     */
    public Step getRelatedStep ()
    {
        return step;
    }

    //----------//
    // getSheet //
    //----------//
    /**
     * Report the model underlying sheet.
     *
     * @return the underlying sheet instance
     */
    public Sheet getSheet ()
    {
        return sheet;
    }

    //----------------//
    // setLatestShape //
    //----------------//
    /**
     * Assign the latest useful shape.
     *
     * @param shape the current / latest shape
     */
    public void setLatestShape (Shape shape)
    {
        if (shape != Shape.GLYPH_PART) {
            latestShape = shape;
        }
    }

    //-------------//
    // assignGlyph //
    //-------------//
    /**
     * Assign a Shape to a glyph, inserting the glyph to its containing
     * system and nest if it is still transient.
     *
     * @param glyph the glyph to be assigned
     * @param shape the assigned shape, which may be null
     * @param grade the grade about shape
     * @return the assigned glyph (perhaps an original glyph)
     */
    protected Glyph assignGlyph (Glyph glyph,
                                 Shape shape,
                                 double grade)
    {
        if (glyph == null) {
            return null;
        }

        if (shape != null) {
            List<SystemInfo> systems = sheet.getSystemManager().getSystemsOf(glyph);

            //            if (system != null) {
            //                glyph = system.registerGlyph(glyph); // System then nest
            //            } else {
            //                // Insert in nest directly, which assigns an id to the glyph
            glyph = nest.registerGlyph(glyph);

            //            }
            boolean isTransient = glyph.isTransient();
            logger.debug(
                    "Assign {}{} to {}",
                    isTransient ? "compound " : "",
                    glyph.idString(),
                    shape);

            // Remember the latest shape assigned
            setLatestShape(shape);
        }

        // Do the assignment of the shape to the glyph
        glyph.setShape(shape);

        // Should we persist the assigned glyph?
        if ((shape != null)
            && (grade == Evaluation.MANUAL)
            && (OMR.getGui() != null)
            && ScoreActions.getInstance().isManualPersisted()) {
            // Record the glyph description to disk
            GlyphRepository.getInstance().recordOneGlyph(glyph, sheet);
        }

        return glyph;
    }

    //---------------//
    // deassignGlyph //
    //---------------//
    /**
     * Deassign the shape of a glyph.
     *
     * @param glyph the glyph to deassign
     */
    protected void deassignGlyph (Glyph glyph)
    {
        // Assign the null shape to the glyph
        assignGlyph(glyph, null, Evaluation.ALGORITHM);
    }

    //-------------//
    // deleteGlyph //
    //-------------//
    protected void deleteGlyph (Glyph glyph)
    {
        logger.error("No yet implemented");

        //        if (glyph == null) {
        //            return;
        //        }
        //
        //        if (!glyph.isVirtual()) {
        //            logger.warn("Attempt to delete non-virtual {}", glyph.idString());
        //
        //            return;
        //        }
        //
        //        SystemInfo system = sheet.getSystemOf(glyph);
        //
        //        // Special case for ledger glyph
        //        if (glyph.getShape() == Shape.LEDGER) {
        //            StaffInfo staff = system.getStaffAt(glyph.getAreaCenter());
        //
        //            ///staff.removeLedger(glyph);
        //            //TODO: handle a LedgerInter instead!
        //        }
        //
        //        if (system != null) {
        //            system.removeGlyph(glyph);
        //        }
        //
        //        nest.removeGlyph(glyph);
    }
}
