package uk.ac.uea.cmp.srnaworkbench.database.exceptions;

/**
 *
 * @author Matthew
 */
public class ChildBeforeParentException extends DatabaseAnnotationException {
//    private static class TypeBeforeReferenceException extends ChildBeforeParentException {

//
//        public TypeBeforeReferenceException(String reference, String type) {
//            super("Adding type " + type + " before its reference sequence " + reference + " is set up and added. "
//                    + "Please add the reference sequence first");
//        }
//    }
//
//    private static class KeywordBeforeTypeException extends ChildBeforeParentException {
//
//        public KeywordBeforeTypeException(String type, String keyword) {
//            super("Adding keyword " + keyword + " before its reference sequence " + type + " is set up and added. "
//                    + "Please add the reference sequence first");
//        }
//    }
    
    public ChildBeforeParentException(String child, String parent) {
        super("Adding child id " + child + " before parent with id " + " is in the database "
                + " please ad its parent first");
    }
    
}
