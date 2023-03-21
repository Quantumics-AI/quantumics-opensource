import pandas as pd
import re
import sys
import os
import traceback

if sys.version_info[0] < 3:
    from StringIO import StringIO # Python 2.x
else:
    from io import StringIO # Python 3.x


def read_xls_file(file_name):
    print("Python received file to convert xlsx to csv :"+file_name)
    status = True
    try:
        df = pd.read_excel(file_name)
        df.columns = [re.sub("\\W", "_", x) for x in df.columns.values.tolist()]
        df.columns = [re.sub("_{2,}", "_", x) for x in df.columns.values.tolist()]
        csv_file_name = ''
        if ".xlsx" in file_name:
            csv_file_name = file_name.replace("xlsx", 'csv')
        else:
            csv_file_name = file_name.replace("xls", 'csv')
        df.to_csv(csv_file_name , index=False)
        os.remove(file_name)
        print("xlsx to csv file convertion is done!, xls file removed from local storage.")
    except Exception as e:
        print('Exception while reading xlsx file :'+str(e))
        traceback.print_exc()
        status = False
    return status

file_apth_with_name = str(sys.argv[1])
response = read_xls_file(file_apth_with_name)
print(str(response))