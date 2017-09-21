package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;

import java.util.EnumMap;

/**
 * A box of trees. :-)
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
final class OriginalTreeBox
{
  private final Tree originalSrnaTree;

  private final EnumMap<Category, CategoryTree> _categoryTreeMap = CollectionUtils.newEnumMap( Category.class );

  public OriginalTreeBox( Tree originalSrnaTree )
  {
    this.originalSrnaTree = originalSrnaTree;

    for ( Category cat : Category.definedCategories() )
    {
      _categoryTreeMap.put( cat, new CategoryTree() );
    }
  }

  public CategoryTree getOriginalCategoryTree( Category cat )
  {
    return _categoryTreeMap.get( cat );
  }

  public CategoryTree getOriginalCategoryTree( int cat )
  {
    return getOriginalCategoryTree( Category.getCategory( cat ) );
  }

  public void setOriginalCategoryTree( Category cat, CategoryTree originalCategoryTree )
  {
    _categoryTreeMap.put( cat, originalCategoryTree );
  }

  public Tree getOriginalSrnaTree()
  {
    return originalSrnaTree;
  }

  public void setOriginalSrnaTreeCategoryTrees()
  {
    originalSrnaTree.setCategoryTrees( this );
  }
}
