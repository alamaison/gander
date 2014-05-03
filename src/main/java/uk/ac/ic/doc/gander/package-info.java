/**
 * Program analysis for Python.
 * 
 * The program is modelled at many different levels and they lie to different
 * degrees:
 * <ul>
 * <li>
 *      Flow analysis - no lies (if there are some minor ones we should add
 *   them here)
 * </li>
 * <li>
 *   Concrete types - implementations lie about which features they support;
 *   they have to pretend features are static in order to allow
 *   contraindication to work
 * </li>
 * </ul>
 */
package uk.ac.ic.doc.gander;

