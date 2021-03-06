/*
 * Copyright (c) 2016 ingenieux Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.ingenieux.mojo.beanstalk.bundle;

import com.amazonaws.auth.AWSCredentialsProvider;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

public class CodeCommitRequestSigner extends RequestSignerBase {
  final String repoName;

  public CodeCommitRequestSigner(AWSCredentialsProvider credentialsProvider, String repoName, Date date) {
    super(credentialsProvider.getCredentials(), "us-east-1", "codecommit", date);
    this.repoName = repoName;
  }

  public String getPushUrl() {
    String user = awsCredentials.getAWSAccessKeyId();

    String host = "git-codecommit.us-east-1.amazonaws.com";

    String path = "/v1/repos/" + repoName;

    String scope = String.format("%s/%s/%s/%s", strDate, region, service, TERMINATOR);

    StringBuilder stringToSign = new StringBuilder();

    stringToSign.append(String.format("%s-%s\n%s\n%s\n", SCHEME, AWS_ALGORITHM, strDateTime, scope));

    stringToSign.append(DigestUtils.sha256Hex(String.format("GIT\n%s\n\nhost:%s\n\nhost\n", path, host).getBytes()));

    byte[] key = deriveKey();

    byte[] digest = hash(key, stringToSign.toString());

    String signature = Hex.encodeHexString(digest);

    String password = strDateTime.concat("Z").concat(signature);

    String returnUrl = String.format("https://%s:%s@%s%s", user, password, host, path);

    return returnUrl;
  }
}
