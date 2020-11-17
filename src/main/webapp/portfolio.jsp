<%
	if(session == null || session.getAttribute("isLoggedIn") == null){
		response.setStatus(response.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", "/login.jsp");
		return;
	}
%>
<!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="head.jsp"%>
	<link rel="stylesheet" href="css/portfolio.css">
</head>
<body>
	<%@include file="appbar.jsp"%>

	<!-- Main content -->
	<div id="app" class="container">
		<!-- Title row -->
		<div class="row">
			<div v-if="username" class="col s6 l5 offset-l1">
				<h5>{{ username }}'s Portfolio</h5>
				<div class="flex">
					<div class="title-value" :style="{'color': deltaRatio >= 0 ? 'green' : 'red'}">{{ '$' + totalValue.toFixed(2) }}</div>
					<div class="flex-column">
						<div class="title-value-text">
							{{ deltaRatio >= 0 ? 'Increase' : 'Decrease' }}
						</div>
						<div class="title-value-percentage" :style="{'color': deltaRatio >= 0 ? 'green' : 'red'}">
							<span v-html="deltaRatio >= 0 ? '	&#9650;' : '&#9660;'"></span> {{ Math.abs(deltaRatio.toFixed(2)) }}%
						</div>
					</div><!-- Delta value since last day -->
				</div><!-- Value and ratio display -->
			</div><!-- Username -->
			<div v-else class="col s6 l5 offset-l1">
				<h4>Loading...</h4>
			</div>
			<div class="col s6 l5">
				<div class="title-addstock right">
					<button class="btn waves-effect waves-light green" type="button"
							@click="openAddStockWindow('portfolio')">Add Stock
						<i class="material-icons right">add</i>
					</button>
				</div>
			</div><!-- Add stock button -->
		</div><!-- Title row -->

		<!-- Chart row -->
		<div class="row">
			<div class="col s12 l10 offset-l1 stock-chart">
				<div id="stock-chart-zoom">
					<button type="button">
						<i class="tiny material-icons" @click="zoom('out')">remove</i>
					</button>
					<button type="button">
						<i class="tiny material-icons" @click="zoom('in')">add</i>
					</button>
				</div>
				<div id="stock-chart"></div>
			</div>
		</div><!-- Chart row -->

		<!-- Stock list row-->
		<div class="row">
			<div class="col s12 l5 offset-l1" style="position: relative;">
				<div class="flex-between list-header">
					<span>Portfolio Stocks</span>
					<button class="btn waves-effect waves-light green right" type="button"
							@click="openPopUp('upload-csv-modal')">Upload Portfolio
						<i class="material-icons right">file_upload</i>
					</button>
				</div>
				<!-- Select all -->
				<div class="list-header">
					<button class="btn-flat waves-effect waves-light" type="button"
							@click="selectAll('portfolio', true)">Select All
					</button>
					<button class="btn-flat waves-effect waves-light" type="button"
							@click="selectAll('portfolio', false)">Unselect All
					</button>
				</div>
				<!-- End of select all -->
				<div id="portfolio-stock-list" class="collection stock-list">
					<div class="collection-item flex-between stock-item" v-for="stock in stocks" :key="stock.id">
						<div class="flex-column">
							<label>
								<input type="checkbox" v-model="stock.checked" @change="updateStockChecked(stock)"/>
								<span>
									<span class="chip" :style="{'color': stock.color}">{{ stock.ticker }}</span>
									<span class="stock-date">
										<span>
											{{ dateString(stock.buyDate) }}
										</span>
										<span v-if="validDate(stock.sellDate)">
											&rarr; {{ dateString(stock.sellDate) }}
										</span>
									</span>
								</span>
							</label>
						</div>
						<div>
							<button class="btn-small red waves-effect waves-light" @click="openPopUp(stock)">
								Delete
							</button><!-- Remove button-->
							<div :id="idByStock(stock)" class="modal">
								<div class="modal-content">
									<h4>Are you sure you want to remove {{stock.ticker}}?</h4>
									<p>{{stock.ticker}} will be removed from your portfolio. This cannot be undone.</p>
								</div>
								<div class="modal-footer">
									<a href="#!" class="waves-effect btn-flat" @click="closePopUp(stock)">Cancel</a>
									<a href="#!" class="btn red waves-effect waves-light" @click="deleteStock(stock)">Delete Stock</a>
								</div>
							</div><!-- Modal Structure -->
						</div>
					</div>
					<div class="collection-item" v-if="stocks.length == 0" key="special-item">
						No stocks found. <a href="javascript:;" @click="openAddStockWindow('portfolio')">Add stock?</a>
					</div>
				</div><!-- Collection -->
			</div><!-- Stocks -->

			<div class="col s12 l5">
				<div class="flex-between list-header">
					<span>Compare historical data</span>
					<button class="btn waves-effect waves-light green right" type="button"
							@click="openAddStockWindow('historical')">View Stock
						<i class="material-icons right">add</i>
					</button>
				</div>
				<!-- Select all -->
				<div class="list-header">
					<button class="btn-flat waves-effect waves-light" type="button"
							@click="selectAll('historical', true)">Select All
					</button>
					<button class="btn-flat waves-effect waves-light" type="button"
							@click="selectAll('historical', false)">Unselect All
					</button>
				</div>
				<!-- End of select all -->
				<div id="historical-stock-list" class="collection stock-list">
					<div class="collection-item flex-between stock-item" v-for="stock in compare" :key="stock.id">
						<div class="flex-column">
							<label>
								<input type="checkbox" v-model="stock.checked" @change="updateStockChecked(stock)"/>
								<span>
									<span class="chip" :style="{'color': stock.color}">{{ stock.ticker }}</span>
								</span>
							</label>
						</div>
						<div>
							<button class="btn-small red waves-effect waves-light" @click="openPopUp(stock)">
								Delete
							</button><!-- Remove button -->
							<div :id="idByStock(stock)" class="modal">
								<div class="modal-content">
									<h4>Are you sure you want to remove {{stock.ticker}}?</h4>
									<p>{{stock.ticker}} will be removed from your historical view.</p>
								</div>
								<div class="modal-footer">
									<a href="#!" class="waves-effect btn-flat" @click="closePopUp(stock)">Cancel</a>
									<a href="#!" class="btn red waves-effect waves-light" @click="deleteCompareStock(stock)">Delete Stock</a>
								</div>
							</div><!-- Modal Structure -->
						</div>
					</div>
					<div class="collection-item" v-if="compare.length == 0" key="special-item">
						<a href="javascript:;" @click="openAddStockWindow('historical')">View stocks</a> to show their
						historical data.
					</div>
				</div><!-- Collection -->
			</div><!-- Compares -->
		</div><!-- Stock list row-->

		<!-- Add stock modal -->
		<form id="add-stock-modal" class="modal" action="javascript:;" @submit="onAddStockFormSubmit">
			<div class="modal-content">
				<h4 class="center-align">{{ addStockTitle }}</h4>
				<!-- Input -->
				<div class="input-field">
					<input id="add-stock-ticker" name="ticker" type="text"
						:class="{ invalid: addstock.error.ticker }"
						v-model="addstock.ticker"
					/>
					<label for="add-stock-ticker">Ticker</label>
					<span class="helper-text" :data-error="addstock.error.ticker || ''"></span>
				</div><!-- Ticker -->
				<div class="input-field">
					<input id="add-stock-shares" name="shares" type="number" step="1"
						:class="{ invalid: addstock.error.quantity }"
						v-model.number="addstock.shares"
					/>
					<label for="add-stock-shares"># of shares</label>
					<span class="helper-text" :data-error="addstock.error.quantity || ''"></span>
				</div><!-- Shares -->
				<div class="input-field">
					<a class="prefix btn-floating waves-effect waves-light" href="javascript:;" @click="openDatePicker('add-stock-bought-date')">
						<i class="material-icons">date_range</i>
					</a>
					<input id="add-stock-bought-date" type="hidden" class="datepicker" @change="updateAddStockDate($event, 'buydate')"/>
					<input id="add-stock-bought" name="buydate" type="text"
						:class="{ invalid: addstock.error.dateBought }"
						v-model="addstock.buydate"
					/>
					<label for="add-stock-bought" :class="{active: addstock.buydate}">Date bought</label>
					<span class="helper-text" :data-error="addstock.error.dateBought || ''"></span>
				</div><!-- Date bought -->
				<div class="input-field">
					<a class="prefix btn-floating waves-effect waves-light" href="javascript:;" @click="openDatePicker('add-stock-sold-date')">
						<i class="material-icons">date_range</i>
					</a>
					<input id="add-stock-sold-date" type="hidden" class="datepicker" @change="updateAddStockDate($event, 'selldate')"/>
					<input id="add-stock-sold" name="selldate" type="text"
						:class="{ invalid: addstock.error.dateSold }"
						v-model="addstock.selldate"
					/>
					<label for="add-stock-sold" :class="{active: addstock.selldate}">Date sold</label>
					<span class="helper-text" :data-error="addstock.error.dateSold || ''"></span>
				</div><!-- Date sold -->
			</div>
			<div class="modal-footer">
				<a href="#!" class="waves-effect btn-flat" @click="closePopUp('add-stock-modal')">Cancel</a>
				<button class="btn green waves-effect waves-light" type="submit">
					{{ addStockConfirm }}
				</button>
			</div>
		</form><!-- Add stock modal -->

		<!-- Upload CSV modal -->
		<form id="upload-csv-modal" class="modal" action="javascript:;" @submit="onUploadFormSubmit">
			<div class="modal-content">
				<h4 class="center-align">Upload Portfolio From File</h4>
				<!-- Input -->
				<div class="file-field input-field">
					<div class="btn">
						Select CSV File
						<i class="material-icons right">file_upload</i>
						<input type="file" name="file">
					</div>
					<div class="file-path-wrapper">
						<input class="file-path" :class="{invalid: upload.error}" type="text">
						<span class="helper-text" :data-error="upload.error || ''" style="height: 40px;"></span>
					</div>
				</div><!-- Ticker -->
			</div>
			<div class="modal-footer">
				<a href="#!" class="waves-effect btn-flat" @click="closePopUp('upload-csv-modal')">Cancel</a>
				<button class="btn green waves-effect waves-light" type="submit">
					Upload File
				</button>
			</div>
		</form><!-- Upload CSV modal -->

		<%@include file="spinner.jsp"%>
	</div><!-- container -->

	<!-- javascript -->
	<script src="js/portfolio/portfolio.js" type="module"></script>
	<!--Highstock library -->
	<script src="https://code.highcharts.com/stock/highstock.js"></script>
	<script src="https://code.highcharts.com/modules/data.js"></script>
	<script src="https://code.highcharts.com/modules/exporting.js"></script>
	<script src="https://code.highcharts.com/modules/export-data.js"></script>
</body>
</html>
