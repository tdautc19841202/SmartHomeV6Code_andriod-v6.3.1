//package cc.wulian.smarthomev6.tst;
//
//import cc.wulian.smarthomev6.proc.MyHousekeeperProc;
//import cc.wulian.smarthomev6.runner.SmartHomeTestHelper;
//import com.wtt.runner.android.IAfterCondition;
//import com.wtt.runner.android.IBeforeCondition;
//
///**
// * Created by 严君 on 2017/6/13.
// *
// * 封装我的管家测试流程
// */
//public class MyMangerTest extends SmartHomeTestHelper implements IBeforeCondition, IAfterCondition {
//
//	private MyHousekeeperProc proc;
//
//	@Override
//	public void before() {
//		proc = new MyHousekeeperProc(getSolo());
//		proc.init();
//	}
//
//	@Override
//	public void after() {
//		if (null != proc) {
//			try {
//				proc.finalize();
//			} catch (Throwable throwable) {
//				throwable.printStackTrace();
//			}
//			proc = null;
//		}
//	}
//
//	@Override
//	public void setUp() throws Exception {
//		setBefore(this);
//		super.setUp();
//	}
//
//	@Override
//	public void tearDown() throws Exception {
//		setAfter(this);
//		super.tearDown();
//	}
//
//	/**
//	 * 创建定时管家任务（默认时间每天9:00），名称小于20个字符
//	 */
//	public void testCreateTimingTask_01() {
//		proc.createTimingTask_01();
//	}
//
//	/**
//	 * 创建定时管家任务(默认星期全选)，名称小于20个字符
//	 */
//	public void testCreateTimingTask_02() {
//		proc.createTimingTask_02();
//	}
//
//	/**
//	 * 创建定时管家任务（默认星期全选，搜索设备）名称小于20个字符
//	 */
//	public void testCreateTimingTask_03() {
//		proc.createTimingTask_03();
//	}
//
//	/**
//	 * 创建定时管家延时任务(默认星期全选)，名称小于20个字符
//	 */
//	public void testCreateTimingTask_04() {
//		proc.createTimingTask_04();
//	}
//
//	/**
//	 * 创建定时管家延时任务(星期不全选)，名称小于20个字符
//	 */
//	public void testCreateTimingTask_05() {
//		proc.createTimingTask_05();
//	}
//
//	/**
//	 * 创建定时管家延时任务（星期未选中）
//	 */
//	public void testCreateTimingTask_06() {
//		proc.createTimingTask_06();
//	}
//
//	/**
//	 * 创建定时管家任务，名称多于20个字符
//	 */
//	public void testCreateTimingTask_07() {
//		proc.createTimingTask_07();
//	}
//
//	/**
//	 * 创建定时任务（场景）（默认时间每天9:00），名称小于20个字符
//	 */
//	public void testCreateTimingTask_08() {
//		proc.createTimingTask_08();
//	}
//
//	/**
//	 * 创建定时任务（场景）（默认星期全选），名称小于20个字符
//	 */
//	public void testCreateTimeTask_09() {
//		proc.createTimeTask_09();
//	}
//
//	/**
//	 * 创建定时管家延时任务（场景）(默认星期全选)，名称小于20个字符
//	 */
//	public void testCreateTimeTask_10() {
//		proc.createTimeTask_10();
//	}
//
//	/**
//	 * 创建定时管家延时任务（场景）(星期不全选)，名称小于20个字符
//	 */
//	public void testCreateTimeTask_11() {
//		proc.createTimeTask_11();
//	}
//
//	/**
//	 * 创建定时管家任务，名称多于20个字符
//	 */
//	public void testCreateTimeTask_12() {
//		proc.createTimeTask_12();
//	}
//
//	/**
//	 * 创建定时任务（场景+设备）
//	 */
//	public void testCreateTimeTask_13() {
//		proc.createTimeTask_13();
//	}
//
//	/**
//	 * 创建情景任务（默认名称，一个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_01() {
//		proc.createSceneTask_01();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_02() {
//		proc.createSceneTask_02();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_03() {
//		proc.createSceneTask_03();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_04() {
//		proc.createSceneTask_04();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_05() {
//		proc.createSceneTask_05();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_06() {
//		proc.createSceneTask_06();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_07() {
//		proc.createSceneTask_07();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_08() {
//		proc.createSceneTask_08();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_09() {
//		proc.createSceneTask_09();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_10() {
//		proc.createSceneTask_10();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_11() {
//		proc.createSceneTask_11();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_12() {
//		proc.createSceneTask_12();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_13() {
//		proc.createSceneTask_13();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_14() {
//		proc.createSceneTask_14();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_15() {
//		proc.createSceneTask_15();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_16() {
//		proc.createSceneTask_16();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_17() {
//		proc.createSceneTask_17();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，非延时任务（设备））
//	 */
//	public void testCreateSceneTask_18() {
//		proc.createSceneTask_18();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_19() {
//		proc.createSceneTask_19();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_20() {
//		proc.createSceneTask_20();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_21() {
//		proc.createSceneTask_21();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，延时任务（设备））
//	 */
//	public void testCreateSceneTask_22() {
//		proc.createSceneTask_22();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_23() {
//		proc.createSceneTask_23();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_24() {
//		proc.createSceneTask_24();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_25() {
//		proc.createSceneTask_25();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_26() {
//		proc.createSceneTask_26();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_27() {
//		proc.createSceneTask_27();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_28() {
//		proc.createSceneTask_28();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_29() {
//		proc.createSceneTask_29();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_30() {
//		proc.createSceneTask_30();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_31() {
//		proc.createSceneTask_31();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_32() {
//		proc.createSceneTask_32();
//	}
//
//	/**
//	 * 创建情景任务（一个触发条件，非默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_33() {
//		proc.createSceneTask_33();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_34() {
//		proc.createSceneTask_34();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_35() {
//		proc.createSceneTask_35();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_36() {
//		proc.createSceneTask_36();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_37() {
//		proc.createSceneTask_37();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，非延时任务（场景））
//	 */
//	public void testCreateSceneTask_38() {
//		proc.createSceneTask_38();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_39() {
//		proc.createSceneTask_39();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_40() {
//		proc.createSceneTask_40();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_41() {
//		proc.createSceneTask_41();
//	}
//
//	/**
//	 * 创建情景任务（多个触发条件，非默认生效时间，延时任务（场景））
//	 */
//	public void testCreateSceneTask_42() {
//		proc.createSceneTask_42();
//	}
//}
