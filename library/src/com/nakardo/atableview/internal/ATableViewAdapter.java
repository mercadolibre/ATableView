package com.nakardo.atableview.internal;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.nakardo.atableview.R;
import com.nakardo.atableview.foundation.NSIndexPath;
import com.nakardo.atableview.internal.ATableViewCellAccessoryView.ATableViewCellAccessoryType;
import com.nakardo.atableview.internal.ATableViewCellDrawable.ATableViewCellBackgroundStyle;
import com.nakardo.atableview.protocol.ATableViewDataSource;
import com.nakardo.atableview.protocol.ATableViewDataSourceExt;
import com.nakardo.atableview.protocol.ATableViewDelegate;
import com.nakardo.atableview.view.ATableView;
import com.nakardo.atableview.view.ATableView.ATableViewStyle;
import com.nakardo.atableview.view.ATableViewCell;
import com.nakardo.atableview.view.ATableViewCell.ATableViewCellSelectionStyle;

public class ATableViewAdapter extends BaseAdapter {
	private List<Integer> mRows;
	private List<List<Integer>> mRowsHeight;
	
	protected ATableView mTableView;

	private void initialize() {
		mRows = new ArrayList<Integer>();
		mRowsHeight = new ArrayList<List<Integer>>();
		
		int sections = 0;
		
		// datasource.
		ATableViewDataSource dataSource = mTableView.getDataSource();
		if (dataSource != null) {
			sections = dataSource.numberOfSectionsInTableView(mTableView);
			for (int s = 0; s < sections; s++) {
				mRows.add(dataSource.numberOfRowsInSection(mTableView, s));
			}
		}
		
		// delegate.
		ATableViewDelegate delegate = mTableView.getDelegate();
		for (int s = 0; s < sections; s++) {
			List<Integer> sectionHeights = new ArrayList<Integer>();
			
			int rows = mRows.get(s);
			for (int r = 0; r < rows; r++) {
				NSIndexPath indexPath = NSIndexPath.indexPathForRowInSection(r, s);
				sectionHeights.add(delegate.heightForRowAtIndexPath(mTableView, indexPath));
			} 
			mRowsHeight.add(sectionHeights);
		}
	}
	
	public ATableViewAdapter(ATableView tableView) {
		mTableView = tableView;
		initialize();
	}
	
	@Override
	public void notifyDataSetChanged() {
		initialize();
		super.notifyDataSetChanged();
	}
	
	public NSIndexPath getIndexPath(int position) {
		int sections = mRows.size();
		for (int s = 0; s < sections; s++) {
			int rows = mRows.get(s);
			if (position < rows) {
				return NSIndexPath.indexPathForRowInSection(position, s);
			}
			position -= rows;
		}
		
		return null;
	}
	
	private boolean isSingleRow(NSIndexPath indexPath) {
		return indexPath.getRow() == 0 && mRows.get(indexPath.getSection()) == 1;
	}
	
	private boolean isTopRow(NSIndexPath indexPath) {
		return indexPath.getRow() == 0 && mRows.get(indexPath.getSection()) > 1;
	}
	
	private boolean isBottomRow(NSIndexPath indexPath) {
		return indexPath.getRow() == mRows.get(indexPath.getSection()) - 1;
	}
	
	private int getRowHeight(NSIndexPath indexPath) {
		Resources res = mTableView.getContext().getResources();
		
		// bottom and single rows have double line, so we've to add extra line to row
		// height to keep same aspect.
		int rowHeight = mRowsHeight.get(indexPath.getSection()).get(indexPath.getRow());
		if (isSingleRow(indexPath) || isBottomRow(indexPath)) {
			rowHeight += (int) ATableViewCellDrawable.CELL_STROKE_WIDTH_DP;
		}
		
		return (int) (rowHeight * res.getDisplayMetrics().density);
	}
	
	private void setupLayout(ATableViewCell cell, NSIndexPath indexPath) {
		Resources res = mTableView.getContext().getResources();
		int rowHeight = getRowHeight(indexPath);
		
		// add margins for grouped style.
		if (mTableView.getStyle() == ATableViewStyle.Grouped) {
			int margin = (int) res.getDimension(R.dimen.atv_style_grouped_margins);
			
			if (isSingleRow(indexPath))  {
				cell.setPadding(margin, margin, margin, margin);
				rowHeight += margin * 2;
			} else if (isTopRow(indexPath)) {
				cell.setPadding(margin, margin, margin, 0);
				rowHeight += margin;
			} else if (isBottomRow(indexPath)) {
				cell.setPadding(margin, 0, margin, margin);
				rowHeight += margin;
			} else {
				cell.setPadding(margin, 0, margin, 0);
			}
		}
		
		ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, rowHeight);
		cell.setLayoutParams(params);
	}
	
	private void setupBackgroundDrawable(ATableViewCell cell, NSIndexPath indexPath) {
		
		// get row style for using specific drawable.
		ATableViewCellBackgroundStyle backgroundStyle = ATableViewCellBackgroundStyle.Middle;
		if (isSingleRow(indexPath)) {
			backgroundStyle = ATableViewCellBackgroundStyle.Single;
		} else if (isTopRow(indexPath)) {
			backgroundStyle = ATableViewCellBackgroundStyle.Top;
		} else if (isBottomRow(indexPath)) {
			backgroundStyle = ATableViewCellBackgroundStyle.Bottom;
		}
		
		// setup background drawables.
		ShapeDrawable normal = new ATableViewCellDrawable(mTableView, backgroundStyle, cell.getBackgroundColor());
		StateListDrawable drawable = new StateListDrawable();
		
		Resources res = mTableView.getContext().getResources();
		int rowHeight = getRowHeight(indexPath);
		
		ATableViewCellSelectionStyle selectionStyle = cell.getSelectionStyle();
		if (selectionStyle != ATableViewCellSelectionStyle.None) {
			int startColor;
			int endColor;
			ShapeDrawable pressed;
			switch (selectionStyle) {
			case Blue:
				startColor = res.getColor(R.color.atv_cell_selection_style_blue_start);
				endColor = res.getColor(R.color.atv_cell_selection_style_blue_end);
				break;
			case Gray:
				startColor = res.getColor(R.color.atv_cell_selection_style_gray_start);
				endColor = res.getColor(R.color.atv_cell_selection_style_gray_end);
				break;
			case HoloBlue:
				startColor = res.getColor(R.color.atv_cell_selection_style_icsblue);
				endColor = res.getColor(R.color.atv_cell_selection_style_icsblue);
				break;
			default:
				startColor = res.getColor(R.color.atv_cell_selection_style_blue_start);
				endColor = res.getColor(R.color.atv_cell_selection_style_blue_end);
				break;
			}
			
			pressed = new ATableViewCellDrawable(mTableView, backgroundStyle, rowHeight, startColor, endColor);
			
			drawable.addState(new int[] { android.R.attr.state_pressed }, pressed);
			drawable.addState(new int[] { android.R.attr.state_focused }, pressed);
		}
		drawable.addState(new int[] {}, normal);
		
		LinearLayout contentView = (LinearLayout)cell.findViewById(R.id.contentView);
		contentView.setBackgroundDrawable(drawable);
	}
	
	private void setupAccessoryButtonDelegateCallback(ATableViewCell cell, final NSIndexPath indexPath) {
		ATableViewCellAccessoryType accessoryType = cell.getAccessoryType();
		if (accessoryType == ATableViewCellAccessoryType.DisclosureButton) {
			View accessoryView = cell.findViewById(R.id.accessoryView);
			accessoryView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ATableViewDelegate delegate = mTableView.getDelegate();
					delegate.accessoryButtonTappedForRowWithIndexPath(mTableView, indexPath);
				}
			});
		}
	}
	
	@Override
	public int getCount() {
		int rows = 0;
		for (int iRows : mRows) rows += iRows;
		
		return rows;
	}

	@Override
	public int getViewTypeCount() {
		ATableViewDataSource dataSource = mTableView.getDataSource();
	    if (dataSource instanceof ATableViewDataSourceExt) {
			return ((ATableViewDataSourceExt) dataSource).numberOfRowStyles();
		}
	    
	    return 1;
	}
	
	@Override
	public int getItemViewType(int position) {
		ATableViewDataSource dataSource = mTableView.getDataSource();
		if (dataSource instanceof ATableViewDataSourceExt) {
			NSIndexPath indexPath = getIndexPath(position);
			return ((ATableViewDataSourceExt) dataSource).styleForRowAtIndexPath(indexPath);			
		}
		
		return 1;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ATableViewDataSource dataSource = mTableView.getDataSource();
		
		ATableViewCell cell = (ATableViewCell)convertView;
		dataSource.setReusableCell(cell);
		
		NSIndexPath indexPath = getIndexPath(position);
		cell = dataSource.cellForRowAtIndexPath(mTableView, indexPath);
		
		setupLayout(cell, indexPath);
		setupBackgroundDrawable(cell, indexPath);
		setupAccessoryButtonDelegateCallback(cell, indexPath);
		
		return cell;
	}
}