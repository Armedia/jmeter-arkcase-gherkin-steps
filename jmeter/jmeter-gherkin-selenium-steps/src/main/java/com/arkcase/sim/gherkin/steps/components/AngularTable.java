/*******************************************************************************
 * #%L
 * Armedia ArkCase
 * %%
 * Copyright (C) 2020 Armedia, LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.arkcase.sim.gherkin.steps.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.arkcase.sim.components.WebDriverHelper.WaitType;
import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.tools.CssClassMatcher;
import com.arkcase.sim.tools.LazyWebElement;

// TODO: Two filters instead of one
// TODO: Drop-down filters (only ever one)
public class AngularTable {

	private static final By GRID_ROOT = By.cssSelector("div.grid.ui-grid");

	private static final By GRID_HEADER = By.cssSelector("div.ui-grid-header");
	private static final By GRID_HEADER_CELLS = By.cssSelector("div.ui-grid-header-cell");
	private static final By GRID_HEADER_CELL_TITLE = By.cssSelector("span.ui-grid-header-cell-label");
	private static final By GRID_HEADER_CELL_SORTER = By.cssSelector("i.ui-grid-icon-angle-down");
	private static final By GRID_HEADER_FILTER = By.cssSelector("input.ui-grid-filter-input");

	private static final By GRID_DATA = By.cssSelector("div.ui-grid-contents-wrapper div.ui-grid-viewport");
	private static final By GRID_ROW = By.cssSelector("div.ui-grid-row");
	private static final By GRID_ROW_CELL = By.cssSelector("div.ui-grid-cell-contents");
	private static final By GRID_ROW_SELECTOR = By.cssSelector("div.ui-grid-selection-row-header-buttons");

	private static final CssClassMatcher GRID_ROW_SELECTED = new CssClassMatcher("ui-grid-row-selected");
	private static final CssClassMatcher GRID_ALL_SELECTED = new CssClassMatcher("ui-grid-all-selected");

	private static final By SORT_MENU = By.cssSelector("div.ui-grid-column-menu ul.ui-grid-menu-items");
	private static final By SORT_MENU_ASC = By.cssSelector("li#menuitem-0 button");
	private static final By SORT_MENU_DESC = By.cssSelector("li#menuitem-1 button");
	private static final By SORT_MENU_NONE = By.cssSelector("li#menuitem-2 button");
	private static final By SORT_MENU_HIDE = By.cssSelector("li#menuitem-3 button");

	private static final By PAGER = By.cssSelector("div.ui-grid-pager-panel");
	private static final By PAGER_FIRST_PAGE = By.cssSelector("button.ui-grid-pager-first");
	private static final By PAGER_PREV_PAGE = By.cssSelector("button.ui-grid-pager-previous");
	private static final By PAGER_CURR_PAGE = By.cssSelector("input.ui-grid-pager-control-input");
	private static final By PAGER_TOTAL_PAGES = By.cssSelector("span.ui-grid-pager-max-pages-number");
	private static final By PAGER_NEXT_PAGE = By.cssSelector("button.ui-grid-pager-next");
	private static final By PAGER_LAST_PAGE = By.cssSelector("button.ui-grid-pager-last");
	private static final By PAGER_PAGE_SIZE = By.cssSelector("div.ui-grid-pager-row-count-picker select");
	private static final By PAGER_STATUS = By.cssSelector("div.ui-grid-pager-count");
	private static final Pattern PAGER_STATUS_PARSER = Pattern
		.compile("^\\s*(\\d+)\\s+\\S+\\s+(\\d+)\\s+of\\s+(\\d+)\\s+items\\s*$", Pattern.CASE_INSENSITIVE);

	public class Pager {

		private final LazyWebElement pager;

		private final LazyWebElement firstPage;
		private final LazyWebElement previousPage;
		private LazyWebElement currentPageElement;
		private int currentPage = 0;
		private LazyWebElement totalPagesElement;
		private int totalPages = 0;
		private final LazyWebElement nextPage;
		private final LazyWebElement lastPage;

		private final Select pageSizeSelect;
		private final List<Integer> supportedPageSizes;
		private int pageSize = 0;

		private final LazyWebElement status;

		private int currentFirstRow = 0;
		private int currentLastRow = 0;
		private int totalRows = 0;

		private Pager() {
			this.pager = new LazyWebElement(AngularTable.this.root, AngularTable.PAGER);

			this.firstPage = new LazyWebElement(this.pager, AngularTable.PAGER_FIRST_PAGE);
			this.previousPage = new LazyWebElement(this.pager, AngularTable.PAGER_PREV_PAGE);
			this.currentPageElement = new LazyWebElement(this.pager, AngularTable.PAGER_CURR_PAGE);
			this.totalPagesElement = new LazyWebElement(this.pager, AngularTable.PAGER_TOTAL_PAGES);
			this.nextPage = new LazyWebElement(this.pager, AngularTable.PAGER_NEXT_PAGE);
			this.lastPage = new LazyWebElement(this.pager, AngularTable.PAGER_LAST_PAGE);
			this.pageSizeSelect = new Select(this.pager.findElement(AngularTable.PAGER_PAGE_SIZE));
			Set<Integer> pageSizes = new LinkedHashSet<>();
			for (WebElement o : this.pageSizeSelect.getOptions()) {
				pageSizes.add(Integer.valueOf(o.getText()));
			}
			this.supportedPageSizes = Collections.unmodifiableList(new ArrayList<>(pageSizes));
			this.status = new LazyWebElement(this.pager, AngularTable.PAGER_STATUS);

			updateStatus();
		}

		public void updateStatus() {
			AngularTable.this.waitHelper.waitForAngular();
			String status = this.status.getText();
			Matcher m = AngularTable.PAGER_STATUS_PARSER.matcher(status);
			if (!m.matches()) {
				throw new RuntimeException(String.format("Failed to parse the string [%s] as a status string", status));
			}

			this.currentFirstRow = Integer.parseInt(m.group(1));
			this.currentLastRow = Integer.parseInt(m.group(2));
			this.totalRows = Integer.parseInt(m.group(3));

			this.currentPage = Integer.valueOf(this.currentPageElement.getAttribute("value"));
			String pages = this.totalPagesElement.getText();
			this.totalPages = Integer.valueOf(pages.replaceAll("[^\\d]", ""));
			this.pageSize = Integer.valueOf(this.pageSizeSelect.getFirstSelectedOption().getText());
		}

		private int movePage(WebElement button) {
			if (!button.isDisplayed() || !button.isEnabled()) {
				AngularTable.this.waitHelper.scrollTo(button);
				AngularTable.this.waitHelper.waitForElement(button, WaitType.CLICKABLE);
			}
			button.click();
			updateStatus();
			return this.currentPage;
		}

		public int firstPage() {
			return movePage(this.firstPage);
		}

		public int previousPage() {
			return movePage(this.previousPage);
		}

		public int currentPage() {
			return this.currentPage;
		}

		private int sanitize(int min, int num, int max) {
			return Math.min(Math.max(min, num), max);
		}

		private int sanitizePage(int page) {
			return sanitize(1, page, this.totalPages);
		}

		public int currentPage(int page) {
			page = sanitizePage(page);
			if (page != this.currentPage) {
				this.currentPageElement.clear();
				this.currentPageElement.sendKeys(String.valueOf(page));
				updateStatus();
			}
			return this.currentPage;
		}

		public int rowsInPage() {
			return ((this.currentLastRow - this.currentFirstRow) + 1);
		}

		public int totalPages() {
			return this.totalPages;
		}

		public int nextPage() {
			return movePage(this.nextPage);
		}

		public int lastPage() {
			return movePage(this.lastPage);
		}

		public List<Integer> supportedPageSizes() {
			return this.supportedPageSizes;
		}

		public void pageSize(int size) {
			if (!this.supportedPageSizes.contains(size)) {
				throw new IllegalArgumentException(
					String.format("Not a supported page size: %d  (supported = %s)", size, this.supportedPageSizes));
			}
			this.pageSizeSelect.selectByVisibleText(String.valueOf(size));
			updateStatus();
			AngularTable.this.rows.clear();
		}

		public Pair<Integer, Integer> currentRange() {
			return Pair.of(this.currentFirstRow, this.currentLastRow);
		}

		public int totalRows() {
			return this.totalRows;
		}

		private int sanitizeRow(int row) {
			return sanitize(1, row, this.totalRows);
		}

		private int sanitizeRowInPage(int row) {
			return sanitize(1, row, this.pageSize);
		}

		/**
		 * <p>
		 * Navigate the pager to the given row, such that the page is moved if necessary, and the
		 * position of the row within the new page is returned (starting at 1).
		 * </p>
		 *
		 * @param row
		 *            the absolute row number to navigate to (between 1 and {@link #totalRows()})
		 * @return the position of the row within the new page
		 */
		public int row(int row) {
			row = sanitizeRow(row);
			// We have to operate 0-based for the math to work
			row--;

			// Which page do we want?
			currentPage((row / this.pageSize) + 1);
			// Now we return the number of row (starting at 1)
			// that the row we want is on
			return (row % this.pageSize) + 1;
		}
	}

	private class ColumnHeader {
		private final int position;
		private final String title;
		private final WebElement sorter;
		private final WebElement filter;

		private ColumnHeader(int position, String title, WebElement sorter, WebElement filter) {
			this.position = position;
			this.title = title;
			this.sorter = sorter;
			this.filter = filter;
		}
	}

	private class Row {
		private final int number;
		private final WebElement element;
		private final Map<Integer, Pair<String, WebElement>> contents;

		private Row(int number, WebElement element) {
			this.number = number;
			this.element = element;

			List<WebElement> cells = element.findElements(AngularTable.GRID_ROW_CELL);
			if (cells.size() != AngularTable.this.headersByPosition.size()) {
				throw new RuntimeException(String.format(
					"Wrong number of cells (%d) found for row # %d on page %d - expected %d", cells.size(), number,
					AngularTable.this.pager.currentPage(), AngularTable.this.headersByPosition.size()));
			}

			// All is well, mine the data...
			Map<Integer, Pair<String, WebElement>> contents = new LinkedHashMap<>();
			int pos = 0;
			for (WebElement cell : cells) {
				pos++;
				contents.put(pos, Pair.of(AngularTable.this.columnHeaders.get(pos - 1), cell));
			}
			this.contents = Collections.unmodifiableMap(contents);
		}

		private WebElement getCellElement(int pos) {
			Pair<String, WebElement> p = this.contents.get(pos);
			if (p == null) {
				throw new NoSuchElementException(
					"Bad column number: " + pos + " (allowed numbers: " + this.contents.keySet() + ")");
			}
			return p.getRight();
		}

		public List<String> getValues() {
			List<String> ret = new ArrayList<>(this.contents.size());
			this.contents.values().stream() //
				.map(Pair::getRight) //
				.map(WebElement::getText) //
				.forEach(ret::add) //
			;
			return ret;
		}

		public Map<String, String> getNamedValues() {
			final Map<String, String> ret = new LinkedHashMap<>();
			this.contents.values().forEach((v) -> {
				String header = v.getKey();
				if (StringUtils.isBlank(header)) { return; }
				ret.put(header, v.getValue().getText());
			});
			return ret;
		}

		private Stream<WebElement> cells() {
			return this.contents.values().stream().map(Pair::getRight);
		}
	}

	private final WaitHelper waitHelper;

	private final LazyWebElement root;

	private final LazyWebElement sortMenu;
	private final LazyWebElement sortMenuAsc;
	private final LazyWebElement sortMenuDesc;
	private final LazyWebElement sortMenuNone;
	private final LazyWebElement sortMenuHide;

	private final LazyWebElement gridData;

	private final Map<Integer, ColumnHeader> headersByPosition;
	private final Map<String, ColumnHeader> headersByName;

	private final WebElement selectAll;
	private final int selectorColumn;

	private final List<String> columnHeaders;

	private final Map<Integer, Row> rows = new TreeMap<>();

	private final Pager pager;

	public AngularTable(WebDriver driver, By root) {
		this(driver, driver.findElement(root));
	}

	public AngularTable(WebDriver driver, WebElement root) {
		this(new WaitHelper(driver), root);
	}

	public AngularTable(WaitHelper helper, By root) {
		this(helper, helper.findElement(root));
	}

	public AngularTable(WaitHelper helper, WebElement root) {
		this.waitHelper = Objects.requireNonNull(helper, "Must provide a non-null WaitHelper instance");
		Objects.requireNonNull(root, "Must provide the root element that houses the grid");
		this.root = new LazyWebElement(root, AngularTable.GRID_ROOT);

		this.sortMenu = new LazyWebElement(this.root, AngularTable.SORT_MENU);
		this.sortMenuAsc = new LazyWebElement(this.sortMenu, AngularTable.SORT_MENU_ASC);
		this.sortMenuDesc = new LazyWebElement(this.sortMenu, AngularTable.SORT_MENU_DESC);
		this.sortMenuNone = new LazyWebElement(this.sortMenu, AngularTable.SORT_MENU_NONE);
		this.sortMenuHide = new LazyWebElement(this.sortMenu, AngularTable.SORT_MENU_HIDE);

		this.gridData = new LazyWebElement(root, AngularTable.GRID_DATA);
		this.pager = new Pager();

		// Parse the row headers...
		WebElement headerBox = root.findElement(AngularTable.GRID_HEADER);

		// Find all the row header cells
		List<String> columnHeaders = new ArrayList<>();
		Map<String, ColumnHeader> headersByName = new LinkedHashMap<>();
		Map<Integer, ColumnHeader> headersByPosition = new LinkedHashMap<>();
		int pos = 0;
		WebElement selectAll = null;
		int selectorColumn = 0;
		for (WebElement headerCell : headerBox.findElements(AngularTable.GRID_HEADER_CELLS)) {
			pos++;

			if (selectAll == null) {
				try {
					selectAll = headerCell.findElement(AngularTable.GRID_ROW_SELECTOR);
					selectorColumn = pos;
				} catch (NoSuchElementException e) {
					selectAll = null;
				}
			}

			WebElement title = null;
			try {
				title = headerCell.findElement(AngularTable.GRID_HEADER_CELL_TITLE);
			} catch (NoSuchElementException e) {
				title = null;
			}

			WebElement sorter = null;
			try {
				sorter = headerCell.findElement(AngularTable.GRID_HEADER_CELL_SORTER);
			} catch (NoSuchElementException e) {
				// Column can't be sorted on
				sorter = null;
			}

			WebElement filter = null;
			try {
				filter = headerCell.findElement(AngularTable.GRID_HEADER_FILTER);
			} catch (NoSuchElementException e) {
				// Column can't be filtered
				filter = null;
			}

			String columnName = StringUtils.EMPTY;
			if (title != null) {
				columnName = StringUtils.strip(title.getText()).replaceAll("\\s+", " ");
				if (StringUtils.isBlank(columnName)) {
					columnName = StringUtils.EMPTY;
				}
			}

			ColumnHeader ch = new ColumnHeader(pos, columnName, sorter, filter);
			if (StringUtils.isNotEmpty(columnName)) {
				headersByName.put(columnName, ch);
			}
			columnHeaders.add(columnName);
			headersByPosition.put(pos, ch);
		}
		this.selectAll = selectAll;
		this.selectorColumn = selectorColumn;
		this.headersByName = Collections.unmodifiableMap(headersByName);
		this.headersByPosition = Collections.unmodifiableMap(headersByPosition);
		this.columnHeaders = Collections.unmodifiableList(columnHeaders);
	}

	public void waitUntilVisible() {
		this.waitHelper.waitForElement(this.root, WaitType.VISIBLE);
	}

	public void waitUntilHidden() {
		this.waitHelper.waitForElement(this.root, WaitType.HIDDEN);
	}

	public int getHeaderPosition(String name) {
		ColumnHeader header = this.headersByName.get(name);
		if (header == null) { throw new NoSuchElementException("No header named [" + name + "]"); }
		return header.position;
	}

	public String getHeaderName(int pos) {
		ColumnHeader header = this.headersByPosition.get(pos);
		if (header == null) { throw new NoSuchElementException("No header in position [" + pos + "]"); }
		return header.title;
	}

	private Row getRow(int rowInPage) {
		return this.rows.computeIfAbsent(this.pager.sanitizeRowInPage(rowInPage), (n) -> {
			// First, find the row within the grid data
			List<WebElement> elements = this.gridData.findElements(AngularTable.GRID_ROW);

			// Find the row within the elements
			if (n > elements.size()) {
				throw new NoSuchElementException(
					String.format("The desired row number (%d) doesn't match the available rows (%d) for page %d", n,
						elements.size(), this.pager.currentPage()));
			}

			// Remember: the number parameter is 1-based...
			return new Row(n, elements.get(n - 1));
		});
	}

	public void update() {
		this.pager.updateStatus();
	}

	public void selectAll() {
		if (this.selectAll != null) {
			if (AngularTable.GRID_ALL_SELECTED.test(this.selectAll)) { return; }
			// Not all selected, so we select all
			this.waitHelper.scrollTo(this.selectAll);
			this.selectAll.click();
			return;
		}

		// Have to select them one at a time...
		for (int i = 1; i <= this.pager.rowsInPage(); i++) {
			select(i);
		}
	}

	public void selectNone() {
		if (this.selectAll != null) {
			if (!AngularTable.GRID_ALL_SELECTED.test(this.selectAll)) {
				selectAll();
			}
			// We togle back the selection
			this.selectAll.click();
			return;
		}

		// Have to select them one at a time...
		for (int i = 1; i <= this.pager.rowsInPage(); i++) {
			unselect(i);
		}
	}

	public void select(int rowInPage) {
		// click on any of the row's cells
		// TODO: This is only for the cells that support direct selection. When checkmarks
		// are in use, the procedure is different.
		rowInPage = this.pager.sanitizeRowInPage(rowInPage);
		Row row = getRow(rowInPage);

		// If the row is already selected, we skip this
		if (AngularTable.GRID_ROW_SELECTED.test(row.element)) { return; }

		WebElement selector = null;
		if (this.selectorColumn > 0) {
			// Show that column, and select it if not selected
			selector = row.getCellElement(this.selectorColumn);
		} else {
			// Find the first cell that's visible
			Optional<WebElement> e = row.cells() //
				.filter(WebElement::isDisplayed) //
				.findFirst() //
			;
			if (!e.isPresent()) {
				throw new ElementNotVisibleException(
					String.format("Unable to find a visible cell to click on for row %d on page %d", rowInPage,
						this.pager.currentPage()));
			}
			selector = e.get();
		}

		// Ok...so click on it!
		this.waitHelper.scrollTo(selector);
		selector.click();
	}

	public void unselect(int rowInPage) {
		// click on any of the row's cells
		// TODO: This is only for the cells that support direct selection. When checkmarks
		// are in use, the procedure is different.
		rowInPage = this.pager.sanitizeRowInPage(rowInPage);
		Row row = getRow(rowInPage);

		// If the row is already unselected, we skip this
		if (!AngularTable.GRID_ROW_SELECTED.test(row.element)) { return; }

		WebElement selector = null;
		if (this.selectorColumn > 0) {
			// Show that column, and select it if not selected
			selector = row.getCellElement(this.selectorColumn);
		} else {
			// Find the first cell that's visible
			Optional<WebElement> e = row.cells() //
				.filter(WebElement::isDisplayed) //
				.findFirst() //
			;
			if (!e.isPresent()) {
				throw new ElementNotVisibleException(
					String.format("Unable to find a visible cell to click on for row %d on page %d", rowInPage,
						this.pager.currentPage()));
			}
			selector = e.get();
		}

		// Ok...so click on it!
		this.waitHelper.scrollTo(selector);
		selector.click();
	}

	public void toggleSelect(int rowInPage) {
		// click on any of the row's cells
		// TODO: This is only for the cells that support direct selection. When checkmarks
		// are in use, the procedure is different.
		rowInPage = this.pager.sanitizeRowInPage(rowInPage);
		Row row = getRow(rowInPage);

		// If the row is already unselected, we skip this
		if (!AngularTable.GRID_ROW_SELECTED.test(row.element)) { return; }

		WebElement selector = null;
		if (this.selectorColumn > 0) {
			// Show that column, and select it if not selected
			selector = row.getCellElement(this.selectorColumn);
		} else {
			// Find the first cell that's visible
			Optional<WebElement> e = row.cells() //
				.filter(WebElement::isDisplayed) //
				.findFirst() //
			;
			if (!e.isPresent()) {
				throw new ElementNotVisibleException(
					String.format("Unable to find a visible cell to click on for row %d on page %d", rowInPage,
						this.pager.currentPage()));
			}
			selector = e.get();
		}

		// Ok...so click on it!
		this.waitHelper.scrollTo(selector);
		selector.click();
	}

	public boolean supportsFilter(String columnName) {
		ColumnHeader header = this.headersByName.get(columnName);
		if (header == null) {
			throw new NoSuchElementException(
				String.format("No header named [%s] was found (possibles = %s)", this.headersByName.keySet()));
		}
		return (header.filter != null);
	}

	public void applyFilter(String columnName, String value) {
		// If sort == null, we remove any sorting
		ColumnHeader header = this.headersByName.get(columnName);
		if (header == null) {
			throw new NoSuchElementException(
				String.format("No header named [%s] was found (possibles = %s)", this.headersByName.keySet()));
		}

		if (header.filter == null) {
			throw new NoSuchElementException(
				String.format("The [%s] header does not support filtering", this.headersByName.keySet()));
		}

		this.waitHelper.scrollTo(header.filter);
		String currentFilter = header.filter.getAttribute("value");

		if (Objects.equals(value, currentFilter)) { return; }
		if (StringUtils.isEmpty(currentFilter) == StringUtils.isEmpty(value)) { return; }

		this.waitHelper.waitForElement(header.filter, WaitType.ENABLED);
		header.filter.clear();
		if (StringUtils.isNotEmpty(value)) {
			header.filter.sendKeys(value);
		}
		this.pager.updateStatus();
	}

	public void clearFilter(String columnName) {
		applyFilter(columnName, null);
	}

	public boolean supportsSort(String columnName) {
		ColumnHeader header = this.headersByName.get(columnName);
		if (header == null) {
			throw new NoSuchElementException(
				String.format("No header named [%s] was found (possibles = %s)", this.headersByName.keySet()));
		}
		return (header.sorter != null);
	}

	public void sortByColumn(String name, Boolean ascending) {
		// If sort == null, we remove any sorting
		ColumnHeader header = this.headersByName.get(name);
		if (header == null) {
			throw new NoSuchElementException(
				String.format("No header named [%s] was found (possibles = %s)", this.headersByName.keySet()));
		}

		WebElement button = null;
		if (ascending == null) {
			button = this.sortMenuNone;
		} else if (ascending) {
			button = this.sortMenuAsc;
		} else {
			button = this.sortMenuDesc;
		}

		// First, show the menu...
		WebElement sortMenuButton = header.sorter;
		if (sortMenuButton == null) {
			throw new UnsupportedOperationException(String.format("The [%s] column doesn't support sorting", name));
		}

		this.waitHelper.scrollTo(sortMenuButton);
		this.waitHelper.waitForElement(sortMenuButton, WaitType.CLICKABLE);
		sortMenuButton.click();
		this.waitHelper.scrollTo(button);
		this.waitHelper.waitForElement(button, WaitType.CLICKABLE);
		button.click();
		this.pager.updateStatus();
	}

	public boolean isRowNotSelected(int rowInPage) {
		return !isRowSelected(rowInPage);
	}

	public boolean isRowSelected(int rowInPage) {
		return AngularTable.GRID_ROW_SELECTED.test(getRow(rowInPage).element);
	}

	public Map<String, String> getNamedValues(int rowInPage) {
		return getRow(rowInPage).getNamedValues();
	}

	public List<String> getValues(int rowInPage) {
		return getRow(rowInPage).getValues();
	}

	public Pager getPager() {
		return this.pager;
	}
}
