package jp.co.sbisec.iris.common.remote.servlet.mock;

import jp.co.sbisec.iris.common.remote.servlet.mock.dto.MockExecIn;
import jp.co.sbisec.iris.common.remote.servlet.mock.dto.MockExecOut;

public class MockService {
	public MockExecOut exec(MockExecIn in){
		MockExecOut out = new MockExecOut();
		out.index=in.index;
		out.title="title"+out.index;
		out.body="body"+out.index;
		return out;
	}
}
