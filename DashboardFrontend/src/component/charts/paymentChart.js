import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Label,
    Cell,
    Legend,
} from "recharts";
import ChartWrapper from "./chartWrapper";

const GREY_THEME_COLORS = [
    "#393838",
    "#4a4949",
    "#5c5b5b",
    "#605b5b",
    "#6c6969",
];

const formatCurrency = (value) => {
    if (value > 1000) {
        return `RM ${(value / 1000).toLocaleString("en-US", {
            minimumFractionDigits: 1,
            maximumFractionDigits: 1,
        })}k`;
    }
    return `RM ${value.toLocaleString("en-US", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    })}`;
};

const formatVolume = (value) => {
    if (value === undefined || value === null) {
        return "0";
    }
    return value.toLocaleString("en-US");
};

// Payment Method Label
const CustomTooltip = ({ active, payload, currentSortKey }) => {
    if (active && payload && payload.length) {
        const data = payload[0].payload;
        const isVolume = currentSortKey === "volume";

        // Logic to switch between count and amount in tooltip
        const successVal = isVolume ? data.success : data.successAmount;
        const failedVal = isVolume ? data.failed : data.failedAmount;
        const totalVal = isVolume ? data.volume : data.amount;

        const formatter = isVolume ? formatVolume : formatCurrency;

        return (
            <div className="chart-tooltip">
                <p className="chart-tooltip-header">{data.category}</p>
                <p className="chart-tooltip-total">
                    Total {isVolume ? "Volume" : "Amount"}: <strong>{formatter(totalVal)}</strong>
                </p>
                <div className="tooltip-stats-row">
                    <div className="stat-box box-success">
                        <span className="stat-label text-success">Success</span>
                        <span className="stat-value text-success">{formatter(successVal)}</span>
                    </div>
                    <div className="stat-box box-failed">
                        <span className="stat-label text-failed">Failed</span>
                        <span className="stat-value text-failed">{formatter(failedVal)}</span>
                    </div>
                </div>
            </div>
        );
    }
    return null;
};

export function PaymentMethodChart({
                                       data,
                                       loading,
                                       title,
                                       error,
                                       icon,
                                       headerActions,
                                       currentSortKey,
                                   }) {
    const isInitialLoading = loading && (!data || data.length === 0);
    const dataFormatter = currentSortKey === "amount" ? formatCurrency : formatVolume;
    const chartData = data || [];

    const isVolume = currentSortKey === "volume";
    const successKey = isVolume ? "success" : "successAmount";
    const failedKey = isVolume ? "failed" : "failedAmount";

    return (
        <ChartWrapper
            title={title}
            loading={isInitialLoading}
            error={error}
            icon={icon}
            headerActions={headerActions}
        >
            <ResponsiveContainer width="100%" height={450}>
                <BarChart
                    data={data}
                    layout="vertical"
                    barGap={5}
                    barCategoryGap="30%"
                    margin={{ top: 20, right: 30, left: 100, bottom: 20 }}
                >
                    <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                    <YAxis
                        dataKey="category"
                        type="category"
                        value="Payment Method"
                        width={180}
                        tickLine={false}
                        axisLine={false}
                        tick={{ fontSize: "150%", fontWeight: 700, fill: "var(--text-muted)" }}
                        dx={-10}
                    />
                    <XAxis
                        type="number"
                        tickFormatter={dataFormatter}
                        tickLine={false}
                        axisLine={false}
                        tick={{ fill: "var(--text-muted)", fontSize: "150%", fontWeight: 700 }}
                    >
                        <Label
                            value={
                                isVolume
                                    ? "Total Volume"
                                    : "Total Amount (MYR)"
                            }
                            position="insideBottom"
                            dy={25}
                            style={{
                                fontWeight: 700,
                                fontSize: "130%",
                                fill: "var(--text-muted)",
                            }}
                        />
                    </XAxis>
                    <Tooltip
                        cursor={{ fill: 'transparent' }}
                        content={<CustomTooltip currentSortKey={currentSortKey} />}
                    />
                    <Bar
                        dataKey={currentSortKey}
                        name="Total"
                        radius={[0, 2, 2, 0]}
                        barSize={5}
                    >
                        {chartData.map((entry, index) => (
                            <Cell
                                key={`cell-${index}`}
                                fill={GREY_THEME_COLORS[index % GREY_THEME_COLORS.length]}
                            />
                        ))}
                    </Bar>
                    <Bar
                        dataKey={successKey}
                        name="Success"
                        fill="#10b981"
                        barSize={15}
                        radius={[0, 2, 2, 0]}
                    />
                    <Bar
                        dataKey={failedKey}
                        name="Failed"
                        fill="#ef4444"
                        barSize={15}
                        radius={[0, 2, 2, 0]}
                    />
                </BarChart>
            </ResponsiveContainer>

            <div
                style={{
                    width: "100%",
                    paddingTop: "15px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: "24px",
                }}
            >
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <span
                      style={{
                          width: "18px",
                          height: "18px",
                          borderRadius: "50%",
                          backgroundColor: "#10b981",
                      }}
                  />
                    <span style={{ fontSize: "150%" }}>
                        Success
                    </span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <span
                      style={{
                          width: "18px",
                          height: "18px",
                          borderRadius: "50%",
                          backgroundColor: "#ef4444",
                      }}
                  />
                    <span style={{ fontSize: "150%"}}>
                        Failed
                    </span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <span
                      style={{
                          width: "18px",
                          height: "18px",
                          borderRadius: "50%",
                          backgroundColor: "#605b5b",
                      }}
                  />
                    <span style={{ fontSize: "150%"}}>
                        Total
                    </span>
                </div>
            </div>
        </ChartWrapper>
    );
}

export default PaymentMethodChart;