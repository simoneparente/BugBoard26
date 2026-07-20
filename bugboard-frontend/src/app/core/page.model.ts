/**
 * Generic Page model that maps Spring Boot's Page<T> structure
 * Used for paginated API responses (0-indexed)
 */
export interface Page<T> {
  /**
   * The page content (array of items)
   */
  content: T[];

  /**
   * Pagination information
   */
  pageable: {
    /**
     * 0-indexed page number (from Spring Boot)
     */
    pageNumber: number;
    /**
     * Number of items per page
     */
    pageSize: number;
  };

  /**
   * Total number of elements across all pages
   */
  totalElements: number;

  /**
   * Total number of pages
   */
  totalPages: number;

  /**
   * Whether this is the first page
   */
  first: boolean;

  /**
   * Whether this is the last page
   */
  last: boolean;

  /**
   * Size of current page
   */
  size: number;

  /**
   * 0-indexed page number
   */
  number: number;
}
