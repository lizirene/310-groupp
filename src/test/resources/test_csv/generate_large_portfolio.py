import pandas as pd
import datetime
import random

columns = [
	"ticker", "quantity", "dateBought", "dateSold"
]
num_rows_per_ticker = 100
ticker_list = [
	"aapl", "goog", "fb", "tsla", "intc"
]
max_quantity = 200
random_date = False
date_boought_range = [datetime.date(2019, 12, 1), datetime.date(2020, 4, 30)]
date_sold_range = [datetime.date(2019, 5, 1), datetime.date(2020, 10, 31)]

file_name = "large.csv"

if __name__ == "__main__":
	df = pd.DataFrame(columns = columns)

	for ticker in ticker_list:
		for i in range(num_rows_per_ticker):
			row = []
			row.append(ticker)
			row.append(random.randrange(1, max_quantity))

			if random_date:
				random_num_days = random.randrange((date_boought_range[1] - date_boought_range[0]).days)
				random_date_bought = date_boought_range[0] + datetime.timedelta(days=random_num_days)
				row.append(random_date_bought.strftime("%m/%d/%Y"))

				random_num_days = random.randrange((date_sold_range[1] - date_sold_range[0]).days)
				random_date_sold = date_sold_range[0] + datetime.timedelta(days=random_num_days)
				row.append(random_date_sold.strftime("%m/%d/%Y"))
			else:
				row.append(date_boought_range[0].strftime("%m/%d/%Y"))
				row.append(date_sold_range[1].strftime("%m/%d/%Y"))

			df.loc[-1] = row
			df.index += 1
			df = df.sort_index()

	# Do not preserve the index column
	df.to_csv(file_name, index=False)

