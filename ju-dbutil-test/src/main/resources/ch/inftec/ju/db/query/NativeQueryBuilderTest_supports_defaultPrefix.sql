select d.id
from DataTypes d
  inner join DataTypes d2 on d2.id = 1
where ${whereClause}
order by ${orderByClause}