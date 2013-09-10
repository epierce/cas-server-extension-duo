package edu.ucr.cnc.cas.support;

/**
 * CasConstants defines several String constants that are used by {@link UsernamePasswordAuthenticationMetaDataPopulator} and
 * {@link edu.ucr.cnc.cas.duo.authentication.DuoAuthenticationMetaDataPopulator} when indicating whether the TGT was issued after
 * authenticating with one or two credentials.
 *
 * @author Michael Kennedy <michael.kennedy@ucr.edu>
 * @version 1.0
 */
public interface CasConstants {
    public final String LOA_ATTRIBUTE = "TGT_LOA";
    public final String LOA_TF = "LOA_TF";
    public final String LOA_SF = "LOA_SF";
}
