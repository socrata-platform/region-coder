@Library('socrata-pipeline-library@3.0.0') _

commonPipeline(
  defaultBuildWorker: 'build-worker',
  jobName: 'region-coder',
  language: 'scala',
  languageVersion: '2.10',
  projects: [
    [
      name: 'region-coder',
      deploymentEcosystem: 'marathon-mesos',
      type: 'service'
    ]
  ],
  teamsChannelWebhookId: 'WORKFLOW_IQ',
)
