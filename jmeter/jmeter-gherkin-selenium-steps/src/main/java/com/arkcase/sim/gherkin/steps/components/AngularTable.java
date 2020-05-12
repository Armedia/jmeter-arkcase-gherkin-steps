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

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.arkcase.sim.components.html.WaitHelper;
import com.arkcase.sim.tools.CssClassMatcher;
import com.arkcase.sim.tools.LazyWebElement;

/*
	Results Grid:
		${container} div.grid.ui-grid

		Headers:
			${grid} div.ui-grid-header

			Row Header Cell:
				${header} div.ui-grid-header-cell div[role="columnheader"]

				Row Header Title:
					${header-cell} span.ui-grid-header-cell-label

				Sort Menu Activator:
					${header-cell} div[role="button"]

		Sort Order Menu:
			${grid} div.ui-grid-column-menu ul.ui-grid-menu-items

			Ascending:
				${sort-order-menu} li#menuitem-0 button

			Descending:
				${sort-order-menu} li#menuitem-1 button

			Remove Sort:
				${sort-order-menu} li#menuitem-2 button

			Hide Column:
				${sort-order-menu} li#menuitem-3 button

		Data Rows:
			${grid} div.ui-grid-contents-wrapper div.ui-grid-viewport

			Row:
				${data-cells} div[role="row"]

				Cell:
					${row} div[role="gridcell"]   <--- click here to select the row

		Pager:
			${grid} div.ui-grid-pager-panel

			First Page:
				${pager} button.ui-grid-pager-first

			Previous Page:
				${pager} button.ui-grid-pager-previous

			Current Page:
				${pager} input.ui-grid-pager-control-input

			Next Page:
				${pager} button.ui-grid-pager-next

			Last Page:
				${pager} button.ui-grid-pager-last

			Pager Select:
				${pager} div.ui-grid-pager-row-count-picker select

				Page Size:
					${pager-select} option:selected

			Pager Status:  ({from} - {to} of {total} items)
				${pager} div.ui-grid-pager-count

		No Results:
			${grid} div[ng-if="showNoData"]
 */

public class AngularTable extends WaitHelper {

	private static final By GRID_ROOT = By.cssSelector("div.grid.ui-grid");

	private static final By GRID_HEADER = By.cssSelector("div.ui-grid-header");
	private static final By GRID_HEADER_CELLS = By.cssSelector("div.ui-grid-header-cell");
	private static final By GRID_HEADER_CELL_TITLE = By.cssSelector("span.ui-grid-header-cell-label");
	private static final By GRID_HEADER_CELL_SORTER = By.cssSelector("i.ui-grid-icon-angle-down");

	private static final By GRID_DATA = By.cssSelector("div.ui-grid-contents-wrapper div.ui-grid-viewport");
	private static final By GRID_ROW = By.cssSelector("div.ui-grid-row");
	private static final By GRID_ROW_CELL = By.cssSelector("div.ui-grid-cell-contents");
	private static final CssClassMatcher GRID_ROW_SELECTED = new CssClassMatcher("ui-grid-row-selected");

	private static final By SORT_MENU = By.cssSelector("div.ui-grid-column-menu ul.ui-grid-menu-items");
	private static final By SORT_MENU_ASC = By.cssSelector("li#menuitem-0 button");
	private static final By SORT_MENU_DESC = By.cssSelector("li#menuitem-1 button");
	private static final By SORT_MENU_NONE = By.cssSelector("li#menuitem-2 button");
	private static final By SORT_MENU_HIDE = By.cssSelector("li#menuitem-3 button");

	private static final By PAGER = By.cssSelector("div.ui-grid-pager-panel");
	private static final By PAGER_FIRST_PAGE = By.cssSelector("button.ui-grid-pager-first");
	private static final By PAGER_PREV_PAGE = By.cssSelector("button.ui-grid-pager-previous");
	private static final By PAGER_CURR_PAGE = By.cssSelector("input.ui-grid-pager-control-input");
	private static final By PAGER_TOTAL_PAGES = By.cssSelector("div.ui-grid-pager-max-pages-number");
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
			waitForAngular();
			String status = this.status.getText();
			Matcher m = AngularTable.PAGER_STATUS_PARSER.matcher(status);
			if (!m.matches()) {
				throw new RuntimeException(String.format("Failed to parse the string [%s] as a status string", status));
			}

			this.currentFirstRow = Integer.parseInt(m.group(1));
			this.currentLastRow = Integer.parseInt(m.group(2));
			this.totalRows = Integer.parseInt(m.group(3));

			this.currentPage = Integer.valueOf(this.currentPageElement.getText());
			String pages = this.totalPagesElement.getText();
			this.totalPages = Integer.valueOf(pages.replaceAll("[^\\d]", ""));
			this.pageSize = Integer.valueOf(this.pageSizeSelect.getFirstSelectedOption().getText());
		}

		private int movePage(WebElement button) {
			if (button.isEnabled()) {
				button.click();
				updateStatus();
			}
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
			AngularTable.this.cells.clear();
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

	private final LazyWebElement root;

	private final LazyWebElement sortMenu;
	private final LazyWebElement sortMenuAsc;
	private final LazyWebElement sortMenuDesc;
	private final LazyWebElement sortMenuNone;
	private final LazyWebElement sortMenuHide;

	private final LazyWebElement gridData;

	private final Map<Integer, Pair<String, WebElement>> headersByPosition;
	private final Map<String, Pair<Integer, WebElement>> headersByName;

	private final Map<Integer, WebElement> rows = new TreeMap<>();
	private final Map<Integer, List<Pair<String, WebElement>>> cells = new TreeMap<>();

	private final Pager pager;

	public AngularTable(WebDriver driver, By root) {
		this(driver, driver.findElement(root));
	}

	public AngularTable(WebDriver driver, WebElement root) {
		super(driver);
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
		Map<String, Pair<Integer, WebElement>> headersByName = new LinkedHashMap<>();
		Map<Integer, Pair<String, WebElement>> headersByPosition = new LinkedHashMap<>();
		int pos = 0;
		for (WebElement headerCell : headerBox.findElements(AngularTable.GRID_HEADER_CELLS)) {
			pos++;
			WebElement title = headerCell.findElement(AngularTable.GRID_HEADER_CELL_TITLE);
			WebElement sorter = headerCell.findElement(AngularTable.GRID_HEADER_CELL_SORTER);
			headersByName.put(title.getText(), Pair.of(pos, sorter));
			headersByPosition.put(pos, Pair.of(title.getText(), sorter));
		}
		this.headersByName = Collections.unmodifiableMap(headersByName);
		this.headersByPosition = Collections.unmodifiableMap(headersByPosition);
	}

	private int getHeaderPosition(String name) {
		Pair<Integer, WebElement> p = this.headersByName.get(name);
		if (p == null) { throw new NoSuchElementException("No header named [" + name + "]"); }
		return p.getLeft();
	}

	private String getHeaderName(int pos) {
		Pair<String, WebElement> p = this.headersByPosition.get(pos);
		if (p == null) { throw new NoSuchElementException("No header in position [" + pos + "]"); }
		return p.getLeft();
	}

	private Map<String, String> constructRow(List<Pair<String, WebElement>> row) {
		Map<String, String> m = new LinkedHashMap<>();
		for (Pair<String, WebElement> p : row) {
			m.put(p.getKey(), p.getValue().getText());
		}
		return m;
	}

	private WebElement getRow(int rowInPage) {
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
			return elements.get(n - 1);
		});
	}

	private List<Pair<String, WebElement>> getRowCells(int rowInPage) {
		return this.cells.computeIfAbsent(this.pager.sanitizeRowInPage(rowInPage), (n) -> {
			// Find the row element
			WebElement rowElement = getRow(n);

			// Now, find its cells...
			List<WebElement> cells = rowElement.findElements(AngularTable.GRID_ROW_CELL);
			if (cells.size() != this.headersByName.size()) {
				throw new RuntimeException(
					String.format("Wrong number of cells (%d) found for row # %d on page %d - expected %d",
						cells.size(), n, this.pager.currentPage(), this.headersByName.size()));
			}

			// All is well, mine the data...
			List<Pair<String, WebElement>> ret = new ArrayList<>(this.headersByName.size());
			for (WebElement cell : cells) {
				ret.add(Pair.of(getHeaderName(ret.size() + 1), cell));
			}
			return ret;
		});
	}

	public void selectRow(int rowInPage) {
		// click on any of the row's cells
		// TODO: This is only for the cells that support direct selection. When checkmarks
		// are in use, the procedure is different.
		rowInPage = this.pager.sanitizeRowInPage(rowInPage);
		List<Pair<String, WebElement>> cells = getRowCells(rowInPage);
		Optional<WebElement> e = cells.stream() //
			.map(Pair::getRight) //
			.filter(WebElement::isDisplayed) //
			.findFirst() //
		;

		if (!e.isPresent()) {
			throw new ElementNotVisibleException(
				String.format("Unable to find a visible cell to click on for row %d on page %d", rowInPage,
					this.pager.currentPage()));
		}

		// Ok...so click on it!
		e.get().click();
	}

	public void sortByColumn(String name, Boolean ascending) {
		// If sort == null, we remove any sorting
		Pair<Integer, WebElement> header = this.headersByName.get(name);
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
		WebElement sortMenuButton = header.getRight();
		waitForElement(sortMenuButton, WaitType.CLICKABLE);
		sortMenuButton.click();
		waitForElement(button, WaitType.CLICKABLE);
		button.click();
		this.pager.updateStatus();
	}

	public boolean isRowNotSelected(int rowInPage) {
		return !isRowSelected(rowInPage);
	}

	public boolean isRowSelected(int rowInPage) {
		return AngularTable.GRID_ROW_SELECTED.test(getRow(rowInPage));
	}

	public Map<String, String> getRowData(int rowInPage) {
		return constructRow(getRowCells(rowInPage));
	}

	public Pager getPager() {
		return this.pager;
	}
}
