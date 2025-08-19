export abstract class TablePaginationBase {
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 5;
  pageSizes: number[] = [3, 5, 10, 20, 50];

  onPageChange(event: any) {
    this.pageSize = event.pageSize;
    this.listItems(event.pageIndex);
  }

  abstract listItems(page: number): void;
}