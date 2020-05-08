package spendreport.v.bounded.n;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spendreport.v.bounded.TransactionSourceBounded;

/**
 * Skeleton code for the datastream walkthrough
 * 配置文件没生效，手动设置配置，代码中设置配置
 * 方便本地地调试，否则，报超时异常
 */
public class ExecutionPlanRun {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutionPlanRun.class);

	public static void main(String[] args) throws Exception {

		int timeount = 1000 * 2000;
		Configuration configuration = new Configuration();
		configuration.setInteger("akka.ask.timeout",timeount);
		configuration.setInteger("web.timeout",timeount);
		configuration.setInteger("heartbeat.timeout",timeount);
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(2,configuration);

		env.setParallelism(1);
		DataStream<Transaction> transactions = env
			.addSource(new TransactionSourceBounded())
			.name("transactions");

		SingleOutputStreamOperator<Transaction> streamOperator1= transactions.process(new ProcessFunction<Transaction, Transaction>() {

			@Override
			public void processElement(Transaction value, Context ctx, Collector<Transaction> out) throws Exception {
				LOG.info("[打印数据1] {}",value.toString());
				out.collect(value);
			}
		})
		;

		streamOperator1.process(new ProcessFunction<Transaction, Transaction>() {

			@Override
			public void processElement(Transaction value, Context ctx, Collector<Transaction> out) throws Exception {
				LOG.info("[打印数据2] {}",value.toString());
				out.collect(value);
			}
		});


		//env.execute("诈骗检测");
		System.out.println("============完成============");

		System.out.println("执行计划:\n"+env.getExecutionPlan().toString());


	}
}
